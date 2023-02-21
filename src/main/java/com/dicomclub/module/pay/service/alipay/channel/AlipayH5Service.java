package com.dicomclub.module.pay.service.alipay.channel;

import com.dicomclub.module.pay.config.AliPayConfig;
import com.dicomclub.module.pay.constants.AliPayConstants;
import com.dicomclub.module.pay.model.PayRequest;
import com.dicomclub.module.pay.model.PayResponse;
import com.dicomclub.module.pay.model.alipay.AliPayApi;
import com.dicomclub.module.pay.model.alipay.AliPayRequest;
import com.dicomclub.module.pay.model.alipay.AliPayResponse;
import com.dicomclub.module.pay.model.alipay.request.AliPayTradeCreateRequest;
import com.dicomclub.module.pay.model.alipay.response.AliPayOrderCreateResponse;
import com.dicomclub.module.pay.service.alipay.AliPayStrategy;
import com.dicomclub.module.pay.service.alipay.common.AliPaySignature;
import com.dicomclub.util.JsonUtil;
import com.dicomclub.util.MapUtil;
import com.dicomclub.util.StringUtil;

import java.io.IOException;
import java.time.LocalDateTime;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.GsonBuilder;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * @author ftm
 * @date 2023/2/17 0017 14:02
 */
public class AlipayH5Service extends AliPayStrategy {
    public AlipayH5Service(AliPayConfig aliPayConfig) {
        super(aliPayConfig);
    }

    @Override
    public PayResponse pay(PayRequest payRequest) {
        AliPayRequest request = (AliPayRequest)payRequest;
        AliPayTradeCreateRequest aliPayOrderQueryRequest = new AliPayTradeCreateRequest();
        aliPayOrderQueryRequest.setAppId(aliPayConfig.getAppId());
        aliPayOrderQueryRequest.setTimestamp(LocalDateTime.now().format(formatter));
        AliPayTradeCreateRequest.BizContent bizContent = new AliPayTradeCreateRequest.BizContent();
        bizContent.setOutTradeNo(request.getOrderId());
        if(request.getOrderAmount()!=null){
            bizContent.setTotalAmount(request.getOrderAmount().doubleValue());
        }
        bizContent.setSubject(request.getOrderName());
        bizContent.setBuyerLogonId(request.getBuyerLogonId());
        bizContent.setBuyerId(request.getBuyerId());
        bizContent.setPassbackParams(request.getAttach());

        //必须传一个
        if (StringUtil.isEmpty(bizContent.getBuyerId())
                && StringUtil.isEmpty(bizContent.getBuyerLogonId())) {
            throw new RuntimeException("alipay.trade.create: buyer_logon_id 和 buyer_id不能同时为空");
        }

        aliPayOrderQueryRequest.setNotifyUrl(aliPayConfig.getNotifyUrl());
        aliPayOrderQueryRequest.setBizContent(JsonUtil.toJsonWithUnderscores(bizContent).replaceAll("\\s*",""));
        aliPayOrderQueryRequest.setSign(AliPaySignature.sign(MapUtil.object2MapWithUnderline(aliPayOrderQueryRequest), aliPayConfig.getPrivateKey()));

        Call<AliPayOrderCreateResponse> call = retrofit.create(AliPayApi.class).tradeCreate((MapUtil.object2MapWithUnderline(aliPayOrderQueryRequest)));
        Response<AliPayOrderCreateResponse> retrofitResponse  = null;
        try{
            retrofitResponse = call.execute();
        }catch (IOException e) {
            e.printStackTrace();
        }
        assert retrofitResponse != null;
        if (!retrofitResponse.isSuccessful()) {
            throw new RuntimeException("【支付宝创建订单】网络异常. alipay.trade.create");
        }
        assert retrofitResponse.body() != null;
        AliPayOrderCreateResponse.AlipayTradeCreateResponse response = retrofitResponse.body().getAlipayTradeCreateResponse();
        if(!response.getCode().equals(AliPayConstants.RESPONSE_CODE_SUCCESS)) {
            throw new RuntimeException("【支付宝创建订单】alipay.trade.create. code=" + response.getCode() + ", returnMsg=" + response.getMsg() + String.format("|%s|%s", response.getSubCode(), response.getSubMsg()));
        }

        AliPayResponse payResponse = new AliPayResponse();
        payResponse.setOutTradeNo(response.getTradeNo());
        payResponse.setOrderId(response.getOutTradeNo());
        return payResponse;
    }



    private Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(AliPayConstants.ALIPAY_GATEWAY_OPEN)
            .addConverterFactory(GsonConverterFactory.create(
                    //下划线驼峰互转
                    new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create()
            ))
            .client(new OkHttpClient.Builder()
                    .addInterceptor((new HttpLoggingInterceptor()
                            .setLevel(HttpLoggingInterceptor.Level.BODY)))
                    .build()
            )
            .build();
}
