package com.dicomclub.payment.module.pay.service.alipay;

import com.alipay.api.AlipayClient;
import com.dicomclub.payment.module.pay.config.AliPayConfig;
import com.dicomclub.payment.module.pay.config.PayConfig;
import com.dicomclub.payment.module.pay.constants.AliPayConstants;
import com.dicomclub.payment.module.pay.enums.*;
import com.dicomclub.payment.module.pay.model.OrderQueryRequest;
import com.dicomclub.payment.module.pay.model.OrderQueryResponse;
import com.dicomclub.payment.module.pay.model.PayRequest;
import com.dicomclub.payment.module.pay.model.PayResponse;
import com.dicomclub.payment.module.pay.model.alipay.AliPayApi;
import com.dicomclub.payment.module.pay.model.alipay.AliPayRequest;
import com.dicomclub.payment.module.pay.model.alipay.AliPayResponse;
import com.dicomclub.payment.module.pay.model.alipay.request.AliPayOrderQueryRequest;
import com.dicomclub.payment.module.pay.model.alipay.request.AliPayPcRequest;
import com.dicomclub.payment.module.pay.model.alipay.response.AliPayAsyncResponse;
import com.dicomclub.payment.module.pay.model.alipay.response.AliPayOrderQueryResponse;
import com.dicomclub.payment.module.pay.service.PayStrategy;
import com.dicomclub.payment.module.pay.service.alipay.channel.AlipayAppService;
import com.dicomclub.payment.module.pay.service.alipay.channel.AlipayBarCodeService;
import com.dicomclub.payment.module.pay.service.alipay.channel.AlipayH5Service;
import com.dicomclub.payment.module.pay.service.alipay.channel.AlipayQRCodeService;
import com.dicomclub.payment.module.pay.service.alipay.common.AliPaySignature;
import com.dicomclub.payment.util.JsonUtil;
import com.dicomclub.payment.util.MapUtil;
import com.dicomclub.payment.util.WebUtil;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.GsonBuilder;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * @author ftm
 * @date 2023/2/15 0015 11:42
 */
@Slf4j
@Component
public class AliPayStrategy extends PayStrategy {

    protected final static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired
    private AlipayH5Service alipayH5Service;

    @Autowired
    private AlipayQRCodeService alipayQRCodeService;

    @Autowired
    private AlipayBarCodeService alipayBarCodeService;

    @Autowired
    private AlipayAppService alipayAppService;


    @Override
    public PayResponse pay(PayRequest payRequest, PayConfig payConfig){

        if(payRequest.getPayChannel() == PayChannel.ALIPAY_H5){
            return alipayH5Service.pay(payRequest,payConfig);
        }else if (payRequest.getPayChannel() == PayChannel.ALIPAY_QRCODE) {
            return alipayQRCodeService.pay(payRequest,payConfig);
        } else if (payRequest.getPayChannel() == PayChannel.ALIPAY_BARCODE) {
            return alipayBarCodeService.pay(payRequest,payConfig);
        }else if (payRequest.getPayChannel() == PayChannel.ALIPAY_APP) {
            return alipayAppService.pay(payRequest,payConfig);
        }
        AliPayConfig aliPayConfig = (AliPayConfig) payConfig;
        AliPayRequest request = (AliPayRequest)payRequest;



//      ??????????????? PC ??? ??????WAP??????
        Map<String, String> requestParams = new HashMap<>();
        requestParams.put("out_trade_no", request.getOrderNo());
        AliPayPcRequest aliPayRequest = new AliPayPcRequest();
        if (request.getPayChannel() == PayChannel.ALIPAY_PC) {
            requestParams.put("product_code", AliPayConstants.FAST_INSTANT_TRADE_PAY);
            aliPayRequest.setMethod(AliPayConstants.ALIPAY_TRADE_PAGE_PAY);
        } else {
            requestParams.put("product_code", AliPayConstants.QUICK_WAP_PAY);
            aliPayRequest.setMethod(AliPayConstants.ALIPAY_TRADE_WAP_PAY);
        }
        requestParams.put("qr_pay_mode", "2");//????????????
        requestParams.put("total_amount", String.valueOf(request.getOrderAmount()));
        requestParams.put("subject", String.valueOf(request.getOrderName()));
        requestParams.put("passback_params", request.getAttach());
        aliPayRequest.setAppId(aliPayConfig.getAppId());
        aliPayRequest.setCharset("UTF-8");
        aliPayRequest.setSignType(AliPayConstants.SIGN_TYPE_RSA2);
        aliPayRequest.setNotifyUrl(aliPayConfig.getNotifyUrl());
        //????????????PayRequest.returnUrl
        aliPayRequest.setReturnUrl(StringUtils.isEmpty(request.getReturnUrl()) ? aliPayConfig.getReturnUrl() : request.getReturnUrl());
        aliPayRequest.setTimestamp(LocalDateTime.now().format(formatter));
        aliPayRequest.setVersion("1.0");
        // ?????????????????????????????????
        aliPayRequest.setBizContent(JsonUtil.toJson(requestParams).replaceAll("\\s*", ""));
        aliPayRequest.setAlipaySdk( "alipay-sdk-java-dynamicVersionNo");
        aliPayRequest.setFormat("json");
        aliPayRequest.setSign(AliPaySignature.sign(MapUtil.object2MapWithUnderline(aliPayRequest), aliPayConfig.getPrivateKey()));

        Map<String, String> parameters = MapUtil.object2MapWithUnderline(aliPayRequest);
        Map<String, String> applicationParams = new HashMap<>();
        applicationParams.put("biz_content", aliPayRequest.getBizContent());
//        parameters.remove("biz_content");
        String baseUrl = WebUtil.getRequestUrl(parameters, aliPayConfig.isSandbox());
        AliPayResponse response = new AliPayResponse();
        if(request.getPayDataType()!=null&&request.getPayDataType() == PayDataType.FORM){
            //      ??????form ,?????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
            parameters.remove("biz_content");
            String body = WebUtil.buildForm(WebUtil.getRequestUrl(parameters, aliPayConfig.isSandbox()), applicationParams);
            response.setFormContent(body);
        }else{
            response.setPayUrl(baseUrl);
        }
        return response;

    }



    /**
     * ????????????
     *
     * @param notifyData
     * @return
     */
    @Override
    public PayResponse asyncNotify(String notifyData,PayConfig payConfig) {
        AliPayConfig aliPayConfig = (AliPayConfig) payConfig;
        try {
            notifyData = URLDecoder.decode(notifyData, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        //????????????
        if (!AliPaySignature.verify(MapUtil.form2Map(notifyData), aliPayConfig.getAliPayPublicKey())) {
            log.error("???????????????????????????????????????????????????, response={}", notifyData);
            throw new RuntimeException("???????????????????????????????????????????????????");
        }
        HashMap<String, String> params = MapUtil.form2MapWithCamelCase(notifyData);
        AliPayAsyncResponse response = MapUtil.mapToObject(params, AliPayAsyncResponse.class);
        String tradeStatus = response.getTradeStatus();


        AliPayResponse payResponse = new AliPayResponse();
        payResponse.setChannelState(ChannelState.WAITING);
        payResponse.setChannelState(AlipayTradeStatusEnum.findByName(tradeStatus).getChannelState());
        if(payResponse.getChannelState() == ChannelState.CONFIRM_FAIL){
            payResponse.setErrCode(tradeStatus);
        }

        if(payResponse.getChannelState() == ChannelState.CONFIRM_SUCCESS){
            return buildPayResponse(response ,payResponse);
        }
//      ?????????????????????
        return payResponse;
    }


    /**
     * ??????
     */
    @Override
    public OrderQueryResponse query(OrderQueryRequest request,PayConfig payConfig) {
        AliPayConfig aliPayConfig = (AliPayConfig) payConfig;
        AliPayOrderQueryRequest aliPayOrderQueryRequest = new AliPayOrderQueryRequest();
        aliPayOrderQueryRequest.setAppId(aliPayConfig.getAppId());
        aliPayOrderQueryRequest.setTimestamp(LocalDateTime.now().format(formatter));
        AliPayOrderQueryRequest.BizContent bizContent = new AliPayOrderQueryRequest.BizContent();
        bizContent.setOutTradeNo(request.getOrderNo());
        bizContent.setTradeNo(request.getOutOrderNo());
        aliPayOrderQueryRequest.setBizContent(JsonUtil.toJsonWithUnderscores(bizContent).replaceAll("\\s*", ""));
        aliPayOrderQueryRequest.setSign(AliPaySignature.sign(MapUtil.object2MapWithUnderline(aliPayOrderQueryRequest), aliPayConfig.getPrivateKey()));

        Call<AliPayOrderQueryResponse> call = null;
        if (aliPayConfig.isSandbox()) {
            call = devRetrofit.create(AliPayApi.class).orderQuery((MapUtil.object2MapWithUnderline(aliPayOrderQueryRequest)));
        } else {
            call = retrofit.create(AliPayApi.class).orderQuery((MapUtil.object2MapWithUnderline(aliPayOrderQueryRequest)));
        }

        Response<AliPayOrderQueryResponse> retrofitResponse = null;
        try {
            retrofitResponse = call.execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
        assert retrofitResponse != null;
        if (!retrofitResponse.isSuccessful()) {
            throw new RuntimeException("???????????????????????????????????????");
        }
        assert retrofitResponse.body() != null;
        AliPayOrderQueryResponse.AlipayTradeQueryResponse response = retrofitResponse.body().getAlipayTradeQueryResponse();
        if (!response.getCode().equals(AliPayConstants.RESPONSE_CODE_SUCCESS)) {
            throw new RuntimeException("???????????????????????????code=" + response.getCode() + ", returnMsg=" + response.getMsg() + String.format("|%s|%s", response.getSubCode(), response.getSubMsg()));
        }

        return OrderQueryResponse.builder()
                .channelState(AlipayTradeStatusEnum.findByName(response.getTradeStatus()).getChannelState())
                .outTradeNo(response.getTradeNo())
                .orderNo(response.getOutTradeNo())
                .resultMsg(response.getMsg())
                .finishTime(response.getSendPayDate())
                .build();
    }






    private Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(AliPayConstants.ALIPAY_GATEWAY_OPEN)
            .addConverterFactory(GsonConverterFactory.create(
                    //?????????????????????
                    new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create()
            ))
            .client(new OkHttpClient.Builder()
                    .addInterceptor((new HttpLoggingInterceptor()
                            .setLevel(HttpLoggingInterceptor.Level.BODY)))
                    .build()
            )
            .build();

    private Retrofit devRetrofit = new Retrofit.Builder()
            .baseUrl(AliPayConstants.ALIPAY_GATEWAY_OPEN_DEV)
            .addConverterFactory(GsonConverterFactory.create(
                    //?????????????????????
                    new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create()
            ))
            .client(new OkHttpClient.Builder()
                    .addInterceptor((new HttpLoggingInterceptor()
                            .setLevel(HttpLoggingInterceptor.Level.BODY)))
                    .build()
            )
            .build();


    private PayResponse buildPayResponse(AliPayAsyncResponse response,AliPayResponse payResponse) {

//        payResponse.setPayType(PayType.ALIPAY);
        payResponse.setOrderAmount(Double.valueOf(response.getTotalAmount()));
        payResponse.setOrderId(response.getOutTradeNo());
        payResponse.setOutTradeNo(response.getTradeNo());
        payResponse.setAttach(response.getPassbackParams());
        return payResponse;
    }


}
