package com.dicomclub.payment.module.pay.service.wxpay;

import com.dicomclub.payment.module.pay.config.PayConfig;
import com.dicomclub.payment.module.pay.config.WxPayConfig;
import com.dicomclub.payment.module.pay.constants.WxPayConstants;
import com.dicomclub.payment.module.pay.enums.*;
import com.dicomclub.payment.module.pay.model.OrderQueryRequest;
import com.dicomclub.payment.module.pay.model.OrderQueryResponse;
import com.dicomclub.payment.module.pay.model.PayRequest;
import com.dicomclub.payment.module.pay.model.PayResponse;
import com.dicomclub.payment.module.pay.model.wxpay.WxPayApi;
import com.dicomclub.payment.module.pay.model.wxpay.WxPayRequest;
import com.dicomclub.payment.module.pay.model.wxpay.WxPayResponse;
import com.dicomclub.payment.module.pay.model.wxpay.request.WxOrderQueryRequest;
import com.dicomclub.payment.module.pay.model.wxpay.request.WxPayUnifiedorderRequest;
import com.dicomclub.payment.module.pay.model.wxpay.response.WxOrderQueryResponse;
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
     * ???????????????
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

        //????????????app??????????????????appid???????????????h5???native??????????????????appid
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
            throw new RuntimeException("????????????????????????????????????, ????????????");
        }
        WxPaySyncResponse response = retrofitResponse.body();

        assert response != null;
        if (!response.getReturnCode().equals(WxPayConstants.SUCCESS)) {
            throw new RuntimeException("????????????????????????????????????, returnCode != SUCCESS, returnMsg = " + response.getReturnMsg());
        }
        if (!response.getResultCode().equals(WxPayConstants.SUCCESS)) {
            throw new RuntimeException("????????????????????????????????????, resultCode != SUCCESS, err_code = " + response.getErrCode() + " err_code_des=" + response.getErrCodeDes());
        }

        return buildPayResponse(response,wxPayConfig);
    }

    /**
     * ????????????
     *
     * @param notifyData
     * @return
     */
    @Override
    public PayResponse asyncNotify(String notifyData ,PayConfig payConfig) {
        WxPayConfig wxPayConfig = (WxPayConfig )payConfig;
        //????????????
        if (!WxPaySignature.verify(XmlUtil.toMap(notifyData), wxPayConfig.getMchKey())) {
            log.error("????????????????????????????????????????????????, response={}", notifyData);
            throw new RuntimeException("????????????????????????????????????????????????");
        }
        //xml???????????????
        WxPayAsyncResponse asyncResponse = (WxPayAsyncResponse) XmlUtil.toObject(notifyData, WxPayAsyncResponse.class);


        WxPayResponse channelResult = new WxPayResponse();
        channelResult.setChannelState(ChannelState.WAITING);
        String channelState = asyncResponse.getReturnCode();
        channelResult.setChannelState(WxTradeStatusEnum.findByName(channelState).getChannelState());
        if(channelResult.getChannelState() == ChannelState.CONFIRM_FAIL){
            channelResult.setErrCode(channelState);
            channelResult.setErrMsg(asyncResponse.getReturnMsg());
        }
//       ????????????????????????
        if(channelResult.getChannelState() == ChannelState.CONFIRM_SUCCESS){
            return buildPayResponse(asyncResponse,channelResult);
        }
//      ?????????????????????
        return channelResult;
    }

    /**
     * ??????????????????
     *
     * @param request
     * @param payConfig
     */
    @Override
    public OrderQueryResponse query(OrderQueryRequest request, PayConfig payConfig) {
        WxPayConfig wxPayConfig = (WxPayConfig )payConfig;
        WxOrderQueryRequest wxRequest = new WxOrderQueryRequest();
        wxRequest.setOutTradeNo(request.getOrderNo());
        wxRequest.setTransactionId(request.getOutOrderNo());

        wxRequest.setAppid(wxPayConfig.getAppId());
        wxRequest.setMchId(wxPayConfig.getMchId());
        wxRequest.setNonceStr(RandomUtil.getRandomStr());
        wxRequest.setSign(WxPaySignature.sign(MapUtil.buildMap(wxRequest), wxPayConfig.getMchKey()));
        RequestBody body = RequestBody.create(MediaType.parse("application/xml; charset=utf-8"), XmlUtil.toString(wxRequest));

        WxPayApi api = null;
        if (wxPayConfig.isSandbox()) {
            api = devRetrofit.create(WxPayApi.class);
        } else {
            api = retrofit.create(WxPayApi.class);
        }
        Call<WxOrderQueryResponse> call = api.orderquery(body);
        Response<WxOrderQueryResponse> retrofitResponse = null;
        try {
            retrofitResponse = call.execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
        assert retrofitResponse != null;
        if (!retrofitResponse.isSuccessful()) {
            throw new RuntimeException("????????????????????????????????????");
        }
        WxOrderQueryResponse response = retrofitResponse.body();

        assert response != null;
        if (!response.getReturnCode().equals(WxPayConstants.SUCCESS)) {
            throw new RuntimeException("????????????????????????returnCode != SUCCESS, returnMsg = " + response.getReturnMsg());
        }
        if (!response.getResultCode().equals(WxPayConstants.SUCCESS)) {
            throw new RuntimeException("????????????????????????resultCode != SUCCESS, err_code = " + response.getErrCode() + ", err_code_des=" + response.getErrCodeDes());
        }

        return OrderQueryResponse.builder()
                .channelState(WxTradeStatusEnum.findByName(response.getTradeState()).getChannelState())
                .resultMsg(response.getTradeStateDesc())
                .outTradeNo(response.getTransactionId())
                .orderNo(response.getOutTradeNo())
                .attach(response.getAttach())
                //yyyyMMddHHmmss -> yyyy-MM-dd HH:mm:ss
                .finishTime(StringUtils.isEmpty(response.getTimeEnd()) ? "" : response.getTimeEnd().replaceAll("(\\d{4})(\\d{2})(\\d{2})(\\d{2})(\\d{2})(\\d{2})", "$1-$2-$3 $4:$5:$6"))
                .build();
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
     * ??????
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
     * ???????????????h5?????????
     *
     * @param response
     * @return
     */
    protected PayResponse buildPayResponse(WxPaySyncResponse response,WxPayConfig wxPayConfig) {
        String timeStamp = String.valueOf(System.currentTimeMillis() / 1000);
        String nonceStr = RandomUtil.getRandomStr();
        String prepayId = response.getPrepayId();

        //?????????????????????map
        Map<String, String> map = new HashMap<>();
        String signType = "MD5";
        map.put("appId", response.getAppid());
        map.put("timeStamp", timeStamp);
        map.put("nonceStr", nonceStr);

        //???????????????
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

        //??????APP????????????????????????prepay_id, package="Sign=WXPay"
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
