package com.dicomclub.payment.module.pay.service.wxpay;

import com.dicomclub.payment.module.pay.config.PayConfig;
import com.dicomclub.payment.module.pay.config.WxPayConfig;
import com.dicomclub.payment.module.pay.constants.WxPayConstants;
import com.dicomclub.payment.module.pay.enums.ChannelState;
import com.dicomclub.payment.module.pay.enums.PayChannel;
import com.dicomclub.payment.module.pay.enums.PayType;
import com.dicomclub.payment.module.pay.model.PayRequest;
import com.dicomclub.payment.module.pay.model.PayResponse;
import com.dicomclub.payment.module.pay.model.wxpay.WxPayApi;
import com.dicomclub.payment.module.pay.model.wxpay.WxPayRequest;
import com.dicomclub.payment.module.pay.model.wxpay.WxPayResponse;
import com.dicomclub.payment.module.pay.model.wxpay.request.WxPayUnifiedorderRequest;
import com.dicomclub.payment.module.pay.model.wxpay.response.WxPayAsyncResponse;
import com.dicomclub.payment.module.pay.model.wxpay.response.WxPaySyncResponse;
import com.dicomclub.payment.module.pay.service.PayStrategy;
import com.dicomclub.payment.module.pay.service.wxpay.channel.WxPayMicroService;
import com.dicomclub.payment.module.pay.service.wxpay.common.WxPaySignature;
import com.dicomclub.payment.util.MapUtil;
import com.dicomclub.payment.util.MoneyUtil;
import com.dicomclub.payment.util.RandomUtil;
import com.dicomclub.payment.util.XmlUtil;
import lombok.extern.slf4j.Slf4j;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.logging.HttpLoggingInterceptor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.simplexml.SimpleXmlConverterFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author ftm
 * @date 2023/2/17 0017 16:22
 */
@Slf4j
@Component
public class WxPayStrategy extends PayStrategy {

    @Autowired
    private WxPayMicroService wxPayMicroService;

    /**
     * 订单预支付
     *
     * @param
     * @return
     */
    @Override
    public PayResponse pay(PayRequest payRequest , PayConfig payConfig) {
        WxPayConfig wxPayConfig =(WxPayConfig) payConfig;
        WxPayRequest request =(WxPayRequest) payRequest;
        if (request.getPayChannel() == PayChannel.WXPAY_MICRO) {
            return wxPayMicroService.pay(request,wxPayConfig);
        }
        WxPayUnifiedorderRequest wxRequest = new WxPayUnifiedorderRequest();
        wxRequest.setOutTradeNo(request.getOrderNo());
        wxRequest.setTotalFee(MoneyUtil.Yuan2Fen(request.getOrderAmount()));
        wxRequest.setBody(request.getOrderName());
        wxRequest.setOpenid(request.getOpenid());
        wxRequest.setTradeType(request.getPayChannel().getCode());

        //小程序和app支付有独立的appid，公众号、h5、native都是公众号的appid
        if (request.getPayChannel() == PayChannel.WXPAY_MINI) {
            wxRequest.setAppid(wxPayConfig.getMiniAppId());
        } else if (request.getPayChannel() == PayChannel.WXPAY_APP) {
            wxRequest.setAppid(wxPayConfig.getAppAppId());
        } else {
            wxRequest.setAppid(wxPayConfig.getAppId());
        }
        wxRequest.setMchId(wxPayConfig.getMchId());
        wxRequest.setNotifyUrl(wxPayConfig.getNotifyUrl());
        wxRequest.setNonceStr(RandomUtil.getRandomStr());
        wxRequest.setSpbillCreateIp(StringUtils.isEmpty(request.getSpbillCreateIp()) ? "8.8.8.8" : request.getSpbillCreateIp());
        wxRequest.setAttach(request.getAttach());
        wxRequest.setSign(WxPaySignature.sign(MapUtil.buildMap(wxRequest), wxPayConfig.getMchKey()));

        wxRequest.setAuthCode("");
        RequestBody body = RequestBody.create(MediaType.parse("application/xml; charset=utf-8"), XmlUtil.toString(wxRequest));

        WxPayApi api = null;
        if (wxPayConfig.isSandbox()) {
            api = devRetrofit.create(WxPayApi.class);
        } else {
            api = retrofit.create(WxPayApi.class);
        }

        Call<WxPaySyncResponse> call = api.unifiedorder(body);
        Response<WxPaySyncResponse> retrofitResponse = null;
        try {
            retrofitResponse = call.execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
        assert retrofitResponse != null;
        if (!retrofitResponse.isSuccessful()) {
            throw new RuntimeException("【微信统一支付】发起支付, 网络异常");
        }
        WxPaySyncResponse response = retrofitResponse.body();

        assert response != null;
        if (!response.getReturnCode().equals(WxPayConstants.SUCCESS)) {
            throw new RuntimeException("【微信统一支付】发起支付, returnCode != SUCCESS, returnMsg = " + response.getReturnMsg());
        }
        if (!response.getResultCode().equals(WxPayConstants.SUCCESS)) {
            throw new RuntimeException("【微信统一支付】发起支付, resultCode != SUCCESS, err_code = " + response.getErrCode() + " err_code_des=" + response.getErrCodeDes());
        }

        return buildPayResponse(response,wxPayConfig);
    }

    /**
     * 异步通知
     *
     * @param notifyData
     * @return
     */
    @Override
    public PayResponse asyncNotify(String notifyData ,PayConfig payConfig) {
        WxPayConfig wxPayConfig = (WxPayConfig )payConfig;
        //签名校验
        if (!WxPaySignature.verify(XmlUtil.toMap(notifyData), wxPayConfig.getMchKey())) {
            log.error("【微信支付异步通知】签名验证失败, response={}", notifyData);
            throw new RuntimeException("【微信支付异步通知】签名验证失败");
        }

        //xml解析为对象
        WxPayAsyncResponse asyncResponse = (WxPayAsyncResponse) XmlUtil.toObject(notifyData, WxPayAsyncResponse.class);


        WxPayResponse channelResult = new WxPayResponse();
        channelResult.setChannelState(ChannelState.WAITING);
        String channelState = asyncResponse.getReturnCode();
        if (WxPayConstants.SUCCESS.equals(channelState)) {
            channelResult.setChannelState(ChannelState.CONFIRM_SUCCESS);
        }else  if("CLOSED".equals(channelState)
                || "REVOKED".equals(channelState)
                || "PAYERROR".equals(channelState)){  //CLOSED—已关闭， REVOKED—已撤销, PAYERROR--支付失败
            channelResult.setChannelState(ChannelState.CONFIRM_FAIL); //支付失败
            channelResult.setErrCode(channelState);
            channelResult.setErrMsg(asyncResponse.getReturnMsg());
        }else
        //该订单已支付直接返回
        if (!asyncResponse.getResultCode().equals(WxPayConstants.SUCCESS)
                && asyncResponse.getErrCode().equals("ORDERPAID")) {
            channelResult.setChannelState(ChannelState.CONFIRM_SUCCESS);
        }else{
            throw new RuntimeException("【微信支付异步通知】未知异常, returnCode = "+channelState+", returnMsg = " + asyncResponse.getReturnMsg());
        }
        return buildPayResponse(asyncResponse,channelResult);
    }





    protected final Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(WxPayConstants.WXPAY_GATEWAY)
            .addConverterFactory(SimpleXmlConverterFactory.create())
            .client(new OkHttpClient.Builder()
                    .addInterceptor((new HttpLoggingInterceptor()
                            .setLevel(HttpLoggingInterceptor.Level.BODY)))
                    .build()
            )
            .build();

    protected final Retrofit devRetrofit = new Retrofit.Builder()
            .baseUrl(WxPayConstants.WXPAY_GATEWAY_SANDBOX)
            .addConverterFactory(SimpleXmlConverterFactory.create())
            .client(new OkHttpClient.Builder()
                    .addInterceptor((new HttpLoggingInterceptor()
                            .setLevel(HttpLoggingInterceptor.Level.BODY)))
                    .build()
            )
            .build();


    /**
     * 异步
     * @param response
     * @return
     */
    private PayResponse buildPayResponse(WxPayAsyncResponse response,WxPayResponse payResponse) {

        payResponse.setReturnCode(response.getReturnCode());
        payResponse.setReturnMsg(response.getReturnMsg());
        payResponse.setResultCode(response.getResultCode());
        payResponse.setErrCode(response.getErrCode());
        payResponse.setErrCodeDes(response.getErrCodeDes());
//        payResponse.setPayType(PayType.WX);
        payResponse.setOrderAmount(MoneyUtil.Fen2Yuan(response.getTotalFee()));
        payResponse.setOrderId(response.getOutTradeNo());
        payResponse.setOutTradeNo(response.getTransactionId());
        payResponse.setAttach(response.getAttach());
        payResponse.setMwebUrl(response.getMwebUrl());
        return payResponse;
    }


    /**
     * 同步返回给h5的参数
     *
     * @param response
     * @return
     */
    protected PayResponse buildPayResponse(WxPaySyncResponse response,WxPayConfig wxPayConfig) {
        String timeStamp = String.valueOf(System.currentTimeMillis() / 1000);
        String nonceStr = RandomUtil.getRandomStr();
        String prepayId = response.getPrepayId();

        //先构造要签名的map
        Map<String, String> map = new HashMap<>();
        String signType = "MD5";
        map.put("appId", response.getAppid());
        map.put("timeStamp", timeStamp);
        map.put("nonceStr", nonceStr);

        //返回的内容
        WxPayResponse payResponse = new WxPayResponse();
        payResponse.setReturnCode(response.getReturnCode());
        payResponse.setReturnMsg(response.getReturnMsg());
        payResponse.setResultCode(response.getResultCode());
        payResponse.setErrCode(response.getErrCode());
        payResponse.setErrCodeDes(response.getErrCodeDes());
        payResponse.setAppId(response.getAppid());
        payResponse.setTimeStamp(timeStamp);
        payResponse.setNonceStr(nonceStr);
        payResponse.setSignType(signType);
        payResponse.setMwebUrl(response.getMwebUrl());
        payResponse.setCodeUrl(response.getCodeUrl());

        //区分APP支付，不需要拼接prepay_id, package="Sign=WXPay"
        if (response.getTradeType().equals(PayChannel.WXPAY_APP.getCode())) {
            String packAge = "Sign=WXPay";
            map.put("package", packAge);
            map.put("prepayid", prepayId);
            map.put("partnerid", response.getMchId());
            payResponse.setPackAge(packAge);
            payResponse.setPaySign(WxPaySignature.signForApp(map, wxPayConfig.getMchKey()));
            payResponse.setPrepayId(prepayId);
            return payResponse;
        } else {
            prepayId = "prepay_id=" + prepayId;
            map.put("package", prepayId);
            map.put("signType", signType);
            payResponse.setPackAge(prepayId);
            payResponse.setPaySign(WxPaySignature.sign(map, wxPayConfig.getMchKey()));
            return payResponse;
        }
    }

}
