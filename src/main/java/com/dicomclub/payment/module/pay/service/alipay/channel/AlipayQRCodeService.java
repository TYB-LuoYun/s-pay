package com.dicomclub.payment.module.pay.service.alipay.channel;

import com.dicomclub.payment.module.pay.config.AliPayConfig;
import com.dicomclub.payment.module.pay.config.PayConfig;
import com.dicomclub.payment.module.pay.constants.AliPayConstants;
import com.dicomclub.payment.module.pay.enums.PayDataType;
import com.dicomclub.payment.module.pay.model.PayRequest;
import com.dicomclub.payment.module.pay.model.PayResponse;
import com.dicomclub.payment.module.pay.model.alipay.AliPayApi;
import com.dicomclub.payment.module.pay.model.alipay.AliPayResponse;
import com.dicomclub.payment.module.pay.model.alipay.request.AliPayTradeCreateRequest;
import com.dicomclub.payment.module.pay.model.alipay.response.AliPayOrderCreateResponse;
import com.dicomclub.payment.module.pay.service.alipay.AliPayStrategy;
import com.dicomclub.payment.module.pay.service.alipay.common.AliPaySignature;
import com.dicomclub.payment.util.MapUtil;
import com.dicomclub.payment.util.JsonUtil;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.GsonBuilder;
import net.glxn.qrgen.core.image.ImageType;
import net.glxn.qrgen.javase.QRCode;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import org.springframework.stereotype.Component;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Base64;

/**
 * @author ftm
 * @date 2023/2/17 0017 15:01
 */
@Component
public class AlipayQRCodeService extends AliPayStrategy {


    @Override
    public PayResponse pay(PayRequest request, PayConfig payConfig) {
        AliPayConfig aliPayConfig = (AliPayConfig) payConfig;
        AliPayTradeCreateRequest aliPayOrderQueryRequest = new AliPayTradeCreateRequest();
        aliPayOrderQueryRequest.setMethod(AliPayConstants.ALIPAY_TRADE_QRCODE_PAY);
        aliPayOrderQueryRequest.setAppId(aliPayConfig.getAppId());
        aliPayOrderQueryRequest.setTimestamp(LocalDateTime.now().format(formatter));
        aliPayOrderQueryRequest.setNotifyUrl(aliPayConfig.getNotifyUrl());
        AliPayTradeCreateRequest.BizContent bizContent = new AliPayTradeCreateRequest.BizContent();
        bizContent.setOutTradeNo(request.getOrderNo());
        if(request.getOrderAmount()!=null){
            bizContent.setTotalAmount(request.getOrderAmount().doubleValue());
        }
        bizContent.setSubject(request.getOrderName());

        aliPayOrderQueryRequest.setBizContent(JsonUtil.toJsonWithUnderscores(bizContent).replaceAll("\\s*", ""));
        aliPayOrderQueryRequest.setSign(AliPaySignature.sign(MapUtil.object2MapWithUnderline(aliPayOrderQueryRequest), aliPayConfig.getPrivateKey()));

        Call<AliPayOrderCreateResponse> call;
        if (aliPayConfig.isSandbox()) {
            call = devRetrofit.create(AliPayApi.class).tradeCreate((MapUtil.object2MapWithUnderline(aliPayOrderQueryRequest)));
        } else {
            call = retrofit.create(AliPayApi.class).tradeCreate((MapUtil.object2MapWithUnderline(aliPayOrderQueryRequest)));
        }
        Response<AliPayOrderCreateResponse> retrofitResponse = null;
        try {
            retrofitResponse = call.execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
        assert retrofitResponse != null;
        if (!retrofitResponse.isSuccessful()) {
            throw new RuntimeException("【支付宝创建订单】网络异常. alipay.trade.precreate");
        }
        assert retrofitResponse.body() != null;
        AliPayOrderCreateResponse.AlipayTradeCreateResponse response = retrofitResponse.body().getAlipayTradePrecreateResponse();
        if (!response.getCode().equals(AliPayConstants.RESPONSE_CODE_SUCCESS)) {
            throw new RuntimeException("【支付宝创建订单】alipay.trade.precreate. code=" + response.getCode() + ", returnMsg=" + response.getMsg() + String.format("|%s|%s", response.getSubCode(), response.getSubMsg()));
        }

        AliPayResponse payResponse = new AliPayResponse();
        payResponse.setOutTradeNo(response.getTradeNo());
        payResponse.setOrderId(response.getOutTradeNo());
        String qrCode = response.getQrCode();
        if(request.getPayDataType() != null && request.getPayDataType() == PayDataType.CODE_URL){
            payResponse.setCodeUrl(qrCode);
        }else{
            //          生成图片
            ByteArrayOutputStream stream = QRCode.from(qrCode).to(ImageType.PNG).stream();
            String base64String = Base64.getEncoder().encodeToString(stream.toByteArray());
            payResponse.setCodeUrl("data:image/png;base64," +base64String);
        }

        return payResponse;
    }

    public static void main(String[] args){

        //          生成图片
        ByteArrayOutputStream stream = QRCode.from("https://qr.alipay.com/bax0921762adutlhfx3n0048").to(ImageType.PNG).stream();

        String base64String = Base64.getEncoder().encodeToString(stream.toByteArray());

        System.out.println("data:image/png;base64," +base64String);

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
