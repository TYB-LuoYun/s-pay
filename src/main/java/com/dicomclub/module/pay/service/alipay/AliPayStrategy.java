package com.dicomclub.module.pay.service.alipay;

import com.dicomclub.exception.ServiceException;
import com.dicomclub.module.pay.config.AliPayConfig;
import com.dicomclub.module.pay.constants.AliPayConstants;
import com.dicomclub.module.pay.enums.PayChannel;
import com.dicomclub.module.pay.enums.PayType;
import com.dicomclub.module.pay.model.PayRequest;
import com.dicomclub.module.pay.model.PayResponse;
import com.dicomclub.module.pay.model.alipay.AliPayRequest;
import com.dicomclub.module.pay.model.alipay.AliPayResponse;
import com.dicomclub.module.pay.model.alipay.request.AliPayPcRequest;
import com.dicomclub.module.pay.model.alipay.response.AliPayAsyncResponse;
import com.dicomclub.module.pay.service.PayStrategy;
import com.dicomclub.module.pay.service.alipay.channel.AlipayAppService;
import com.dicomclub.module.pay.service.alipay.channel.AlipayBarCodeService;
import com.dicomclub.module.pay.service.alipay.channel.AlipayH5Service;
import com.dicomclub.module.pay.service.alipay.channel.AlipayQRCodeService;
import com.dicomclub.module.pay.service.alipay.common.AliPaySignature;
import com.dicomclub.util.JsonUtil;
import com.dicomclub.util.MapUtil;
import com.dicomclub.util.WebUtil;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.GsonBuilder;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import org.apache.commons.lang3.StringUtils;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

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
public class AliPayStrategy extends PayStrategy{

    protected final static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    protected AliPayConfig aliPayConfig;


    public AliPayStrategy(AliPayConfig aliPayConfig){
        this.aliPayConfig = aliPayConfig;
    }


    @Override
    public PayResponse pay(PayRequest payRequest){
        AliPayRequest request = (AliPayRequest)payRequest;
        if(payRequest.getPayChannel() == PayChannel.ALIPAY_H5){
            AlipayH5Service alipayH5Service = new AlipayH5Service(aliPayConfig);
            return alipayH5Service.pay(payRequest);
        }else if (payRequest.getPayChannel() == PayChannel.ALIPAY_QRCODE) {
            AlipayQRCodeService alipayQRCodeService = new AlipayQRCodeService(aliPayConfig);
            return alipayQRCodeService.pay(payRequest);
        } else if (payRequest.getPayChannel() == PayChannel.ALIPAY_BARCODE) {
            AlipayBarCodeService alipayBarCodeService = new AlipayBarCodeService(aliPayConfig);
            return alipayBarCodeService.pay(payRequest);
        }else if (payRequest.getPayChannel() == PayChannel.ALIPAY_APP) {
            AlipayAppService alipayAppService = new AlipayAppService(aliPayConfig);
            return alipayAppService.pay(payRequest);
        }



//      接下来就是 PC 和 手机WAP支付
        Map<String, String> requestParams = new HashMap<>();
        requestParams.put("out_trade_no", request.getOrderId());
        AliPayPcRequest aliPayRequest = new AliPayPcRequest();
        if (request.getPayChannel() == PayChannel.ALIPAY_PC) {
            requestParams.put("product_code", AliPayConstants.FAST_INSTANT_TRADE_PAY);
            aliPayRequest.setMethod(AliPayConstants.ALIPAY_TRADE_PAGE_PAY);
        } else {
            requestParams.put("product_code", AliPayConstants.QUICK_WAP_PAY);
            aliPayRequest.setMethod(AliPayConstants.ALIPAY_TRADE_WAP_PAY);
        }
        requestParams.put("total_amount", String.valueOf(request.getOrderAmount()));
        requestParams.put("subject", String.valueOf(request.getOrderName()));
        requestParams.put("passback_params", request.getAttach());

        aliPayRequest.setAppId(aliPayConfig.getAppId());
        aliPayRequest.setCharset("utf-8");
        aliPayRequest.setSignType(AliPayConstants.SIGN_TYPE_RSA2);
        aliPayRequest.setNotifyUrl(aliPayConfig.getNotifyUrl());
        //优先使用PayRequest.returnUrl
        aliPayRequest.setReturnUrl(StringUtils.isEmpty(request.getReturnUrl()) ? aliPayConfig.getReturnUrl() : request.getReturnUrl());
        aliPayRequest.setTimestamp(LocalDateTime.now().format(formatter));
        aliPayRequest.setVersion("1.0");
        // 剔除空格、制表符、换行
        aliPayRequest.setBizContent(JsonUtil.toJson(requestParams).replaceAll("\\s*", ""));
        aliPayRequest.setSign(AliPaySignature.sign(MapUtil.object2MapWithUnderline(aliPayRequest), aliPayConfig.getPrivateKey()));

        Map<String, String> parameters = MapUtil.object2MapWithUnderline(aliPayRequest);
        Map<String, String> applicationParams = new HashMap<>();
        applicationParams.put("biz_content", aliPayRequest.getBizContent());
        parameters.remove("biz_content");
        String baseUrl = WebUtil.getRequestUrl(parameters, aliPayConfig.isSandbox());
        String body = WebUtil.buildForm(baseUrl, applicationParams);

        // pc 网站支付 只需返回body
        AliPayResponse response = new AliPayResponse();
        response.setBody(body);
        return response;

        
    }



    /**
     * 异步通知
     *
     * @param notifyData
     * @return
     */
    @Override
    public PayResponse asyncNotify(String notifyData) {
        try {
            notifyData = URLDecoder.decode(notifyData, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        //签名校验
        if (!AliPaySignature.verify(MapUtil.form2Map(notifyData), aliPayConfig.getAliPayPublicKey())) {
            log.error("【支付宝支付异步通知】签名验证失败, response={}", notifyData);
            throw new RuntimeException("【支付宝支付异步通知】签名验证失败");
        }
        HashMap<String, String> params = MapUtil.form2MapWithCamelCase(notifyData);
        AliPayAsyncResponse response = MapUtil.mapToObject(params, AliPayAsyncResponse.class);
        String tradeStatus = response.getTradeStatus();
        if (!tradeStatus.equals(AliPayConstants.TRADE_FINISHED) &&
                !tradeStatus.equals(AliPayConstants.TRADE_SUCCESS)) {
            throw new RuntimeException("【支付宝支付异步通知】发起支付, trade_status != SUCCESS | FINISHED");
        }
        return buildPayResponse(response);
    }


    /**
     * 退款
     */






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

    private Retrofit devRetrofit = new Retrofit.Builder()
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


    private PayResponse buildPayResponse(AliPayAsyncResponse response) {
        AliPayResponse payResponse = new AliPayResponse();
        payResponse.setPayType(PayType.ALIPAY);
        payResponse.setOrderAmount(Double.valueOf(response.getTotalAmount()));
        payResponse.setOrderId(response.getOutTradeNo());
        payResponse.setOutTradeNo(response.getTradeNo());
        payResponse.setAttach(response.getPassbackParams());
        return payResponse;
    }


}
