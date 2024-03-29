package com.dicomclub.payment.module.pay.service.alipay.channel;

import com.dicomclub.payment.module.pay.config.AliPayConfig;
import com.dicomclub.payment.module.pay.config.PayConfig;
import com.dicomclub.payment.module.pay.constants.AliPayConstants;
import com.dicomclub.payment.module.pay.model.PayRequest;
import com.dicomclub.payment.module.pay.model.PayResponse;
import com.dicomclub.payment.module.pay.model.alipay.AliPayResponse;
import com.dicomclub.payment.module.pay.model.alipay.request.AliPayTradeCreateRequest;
import com.dicomclub.payment.module.pay.service.alipay.AliPayStrategy;
import com.dicomclub.payment.module.pay.service.alipay.common.AliPaySignature;
import com.dicomclub.payment.util.JsonUtil;
import com.dicomclub.payment.util.MapUtil;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.GsonBuilder;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import org.springframework.stereotype.Component;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.net.URLEncoder;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * @author ftm
 * @date 2023/2/17 0017 14:02
 */
@Component
public class AlipayAppService extends AliPayStrategy {



    @Override
    public PayResponse pay(PayRequest request, PayConfig payConfig) {
        AliPayConfig aliPayConfig = (AliPayConfig) payConfig;
        AliPayTradeCreateRequest aliPayOrderQueryRequest = new AliPayTradeCreateRequest();
        aliPayOrderQueryRequest.setMethod(AliPayConstants.ALIPAY_TRADE_APP_PAY);
        aliPayOrderQueryRequest.setAppId(aliPayConfig.getAppId());
        aliPayOrderQueryRequest.setTimestamp(LocalDateTime.now().format(formatter));
        aliPayOrderQueryRequest.setNotifyUrl(aliPayConfig.getNotifyUrl());
        AliPayTradeCreateRequest.BizContent bizContent = new AliPayTradeCreateRequest.BizContent();
        bizContent.setOutTradeNo(request.getOrderNo());
        if(request.getOrderAmount()!=null){
            bizContent.setTotalAmount(request.getOrderAmount().doubleValue());
        }
        bizContent.setSubject(request.getOrderName());
        bizContent.setPassbackParams(request.getAttach());

        aliPayOrderQueryRequest.setBizContent(JsonUtil.toJsonWithUnderscores(bizContent).replaceAll("\\s*", ""));
        String sign = AliPaySignature.sign(MapUtil.object2MapWithUnderline(aliPayOrderQueryRequest), aliPayConfig.getPrivateKey());
        aliPayOrderQueryRequest.setSign(URLEncoder.encode(sign));

        Map<String, String> stringStringMap = MapUtil.object2MapWithUnderline(aliPayOrderQueryRequest);
        String body = MapUtil.toUrl(stringStringMap);
        PayResponse payResponse = new AliPayResponse();
        payResponse.setBody(body);
        return payResponse;
    }


    private final Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(AliPayConstants.ALIPAY_GATEWAY_OPEN)
            .addConverterFactory(GsonConverterFactory.create(
                    //下划线驼峰互转
                    new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create()
            ))
            .client(new OkHttpClient.Builder()
                    .addInterceptor((new HttpLoggingInterceptor()
                            .setLevel(HttpLoggingInterceptor.Level.BODY)))
                    .followRedirects(false)  //禁制OkHttp的重定向操作，我们自己处理重定向
                    .followSslRedirects(false)
                    .build()
            )
            .build();

    private final Retrofit devRetrofit = new Retrofit.Builder()
            .baseUrl(AliPayConstants.ALIPAY_GATEWAY_OPEN_DEV)
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
