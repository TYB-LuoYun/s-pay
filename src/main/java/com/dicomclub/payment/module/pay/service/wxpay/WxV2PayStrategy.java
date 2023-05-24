package com.dicomclub.payment.module.pay.service.wxpay;

import com.alibaba.fastjson2.JSON;
import com.dicomclub.payment.exception.PayException;
import com.dicomclub.payment.module.pay.common.ChannelStateRes;
import com.dicomclub.payment.module.pay.config.PayConfig;
import com.dicomclub.payment.module.pay.config.WxPayConfig;
import com.dicomclub.payment.module.pay.constants.WxPayConstants;
import com.dicomclub.payment.module.pay.enums.*;
import com.dicomclub.payment.module.pay.model.*;
import com.dicomclub.payment.module.pay.model.wxpay.*;
import com.dicomclub.payment.module.pay.model.wxpay.request.WxOrderQueryRequest;
import com.dicomclub.payment.module.pay.model.wxpay.request.WxPayUnifiedorderRequest;
import com.dicomclub.payment.module.pay.model.wxpay.response.WxOrderQueryResponse;
import com.dicomclub.payment.module.pay.model.wxpay.response.WxPayAsyncResponse;
import com.dicomclub.payment.module.pay.model.wxpay.response.WxPaySyncResponse;
import com.dicomclub.payment.module.pay.service.PayStrategy;
import com.dicomclub.payment.module.pay.service.wxpay.v2.channel.WxV2PayMicroService;
import com.dicomclub.payment.module.pay.service.wxpay.v2.common.WxPaySignature;
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
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.simplexml.SimpleXmlConverterFactory;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author ftm
 * @date 2023/2/17 0017 16:22
 */
@Slf4j
@Component
@Primary
public class WxV2PayStrategy extends PayStrategy {

    @Autowired
    private WxV2PayMicroService wxPayMicroService;

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
     * @return
     */
    @Override
    public PayResponse asyncNotify(HttpServletRequest request, PayConfig payConfig) {
        String notifyData = null;
        try {
            notifyData = request.getReader().lines().collect(Collectors.joining());
        } catch (IOException e) {
            throw new PayException(e.getMessage());
        }
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
        channelResult.setChannelState(WxTradeStatusEnum.findByName(channelState).getChannelState());
        if(channelResult.getChannelState() == ChannelState.CONFIRM_FAIL){
            channelResult.setCode(channelState);
            channelResult.setMsg(asyncResponse.getReturnMsg());
        }
//       其他情况都非终态
        if(channelResult.getChannelState() == ChannelState.CONFIRM_SUCCESS){
            return buildPayResponse(asyncResponse,channelResult);
        }
//      其他情况非终态
        return channelResult;
    }

    /**
     * 异步通知-退款
     * 与支付结果不同的是，退款结果可能会受到更多的影响因素，比如退款金额、退款原因、退款渠道等，因此退款处理的时间可能会比支付处理更长。通过异步回调接口，商户可以及时获取退款结果，以便及时处理相关业务。
     *
     * @param request
     * @param payConfig
     * @return
     */
    @Override
    public RefundResponse asyncNotifyRefund(HttpServletRequest request, PayConfig payConfig) {
        return null;
    }

    /**
     * 异步通知-退款
     * 与支付结果不同的是，退款结果可能会受到更多的影响因素，比如退款金额、退款原因、退款渠道等，因此退款处理的时间可能会比支付处理更长。通过异步回调接口，商户可以及时获取退款结果，以便及时处理相关业务。
     *
     * @param request
     * @param payConfig
     * @return
     */


    /**
     * 订单结果查询
     *
     * @param request
     * @param payConfig
     */
    @Override
    public PayResponse query(OrderQueryRequest request, PayConfig payConfig) {
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
            throw new RuntimeException("【微信订单查询】网络异常");
        }
        WxOrderQueryResponse response = retrofitResponse.body();

        assert response != null;
        if (!response.getReturnCode().equals(WxPayConstants.SUCCESS)) {
            throw new RuntimeException("【微信订单查询】returnCode != SUCCESS, returnMsg = " + response.getReturnMsg());
        }
        if (!response.getResultCode().equals(WxPayConstants.SUCCESS)) {
            throw new RuntimeException("【微信订单查询】resultCode != SUCCESS, err_code = " + response.getErrCode() + ", err_code_des=" + response.getErrCodeDes());
        }


        WxPayResponse wxPayResponse = new WxPayResponse();
        wxPayResponse.setChannelState(WxTradeStatusEnum.findByName(response.getTradeState()).getChannelState());
        wxPayResponse.setMsg(response.getTradeStateDesc());
        wxPayResponse.setOutTradeNo(response.getTransactionId());
        wxPayResponse.setOrderNo(response.getOutTradeNo());
        wxPayResponse.setAttach(response.getAttach());
        wxPayResponse.setFinishTime(StringUtils.isEmpty(response.getTimeEnd()) ? "" : response.getTimeEnd().replaceAll("(\\d{4})(\\d{2})(\\d{2})(\\d{2})(\\d{2})(\\d{2})", "$1-$2-$3 $4:$5:$6"));
        return wxPayResponse;
    }

    /**
     * 退款
     *
     * @param request
     * @param payConfig
     */
    @Override
    public RefundResponse refund(RefundRequest request, PayConfig payConfig) {
        return null;
    }

    /**
     * 退款查询
     *
     * @param refundNo
     * @param payConfig
     */
    @Override
    public RefundResponse refundQuery(RefundQueryRequest refundNo, PayConfig payConfig) {
        return null;
    }


    /**
     * 转账
     *
     * @param order
     * @param payConfig
     */
    @Override
    public TransferResponse transfer(TransferOrder order, PayConfig payConfig) {
        return null;
    }

    @Override
    public TransferResponse transferQuery(TransferQueryRequest request, PayConfig payConfig) {
        return null;
    }

    /**
     * 分账
     *
     * @param request
     * @param payConfig
     */
    @Override
    public DivisionResponse division(DivisionRquest request, PayConfig payConfig) {
        return null;
    }

    @Override
    public DivisionResponse divisionQuery(DivisionQueryRquest divisionQueryRquest, PayConfig payConfig) {
        return null;
    }

    @Override
    public ChannelStateRes divisionBind(DivisionReceiverBind divisionReceiverBind, PayConfig payConfig) {
        return null;
    }

    @Override
    public BillResponse downloadBill(BillRequest downloadBillRequest, PayConfig payConfig) {
        return null;
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

//        payResponse.setReturnCode(response.getReturnCode());
//        payResponse.setReturnMsg(response.getReturnMsg());
//        payResponse.setResultCode(response.getResultCode());
//        payResponse.setErrCode(response.getErrCode());
//        payResponse.setErrCodeDes(response.getErrCodeDes());
////        payResponse.setPayType(PayType.WX);
//        payResponse.setOrderAmount(MoneyUtil.Fen2Yuan(response.getTotalFee()));
//        payResponse.setOrderNo(response.getOutTradeNo());
//        payResponse.setOutTradeNo(response.getTransactionId());
//        payResponse.setAttach(response.getAttach());
//        payResponse.setMwebUrl(response.getMwebUrl());
        payResponse.setDataMap(JSON.parseObject(JSON.toJSONString(response)));
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
//        payResponse.setReturnCode(response.getReturnCode());
//        payResponse.setReturnMsg(response.getReturnMsg());
//        payResponse.setResultCode(response.getResultCode());
//        payResponse.setErrCode(response.getErrCode());
//        payResponse.setErrCodeDes(response.getErrCodeDes());
//        payResponse.setAppId(response.getAppid());
//        payResponse.setTimeStamp(timeStamp);
//        payResponse.setNonceStr(nonceStr);
//        payResponse.setSignType(signType);
//        payResponse.setMwebUrl(response.getMwebUrl());
        payResponse.setCodeUrl(response.getCodeUrl());
        payResponse.setDataMap(JSON.parseObject(JSON.toJSONString(response)));

        //区分APP支付，不需要拼接prepay_id, package="Sign=WXPay"
        if (response.getTradeType().equals(PayChannel.WXPAY_APP.getCode())) {
            String packAge = "Sign=WXPay";
            map.put("package", packAge);
            map.put("prepayid", prepayId);
            map.put("partnerid", response.getMchId());
//            payResponse.setPackAge(packAge);
//            payResponse.setPaySign(WxPaySignature.signForApp(map, wxPayConfig.getMchKey()));
//            payResponse.setPrepayId(prepayId);
            payResponse.setDataMap(JSON.parseObject(JSON.toJSONString(response)));
            return payResponse;
        } else {
            prepayId = "prepay_id=" + prepayId;
            map.put("package", prepayId);
            map.put("signType", signType);
//            payResponse.setPackAge(prepayId);
//            payResponse.setPaySign(WxPaySignature.sign(map, wxPayConfig.getMchKey()));
            payResponse.setDataMap(JSON.parseObject(JSON.toJSONString(response)));
            return payResponse;
        }
    }

}
