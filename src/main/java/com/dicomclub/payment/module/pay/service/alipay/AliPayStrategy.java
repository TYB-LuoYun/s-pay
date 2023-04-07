package com.dicomclub.payment.module.pay.service.alipay;

import com.alipay.api.AlipayClient;
import com.dicomclub.payment.exception.PayException;
import com.dicomclub.payment.module.pay.common.ChannelStateRes;
import com.dicomclub.payment.module.pay.config.AliPayConfig;
import com.dicomclub.payment.module.pay.config.PayConfig;
import com.dicomclub.payment.module.pay.constants.AliPayConstants;
import com.dicomclub.payment.module.pay.enums.*;
import com.dicomclub.payment.module.pay.model.*;
import com.dicomclub.payment.module.pay.model.alipay.AliPayApi;
import com.dicomclub.payment.module.pay.model.alipay.AliPayRequest;
import com.dicomclub.payment.module.pay.model.alipay.AliPayResponse;
import com.dicomclub.payment.module.pay.model.alipay.request.AliPayBankRequest;
import com.dicomclub.payment.module.pay.model.alipay.request.AliPayOrderQueryRequest;
import com.dicomclub.payment.module.pay.model.alipay.request.AliPayOrderRefundRequest;
import com.dicomclub.payment.module.pay.model.alipay.request.AliPayPcRequest;
import com.dicomclub.payment.module.pay.model.alipay.response.AliPayAsyncResponse;
import com.dicomclub.payment.module.pay.model.alipay.response.AliPayBankResponse;
import com.dicomclub.payment.module.pay.model.alipay.response.AliPayOrderQueryResponse;
import com.dicomclub.payment.module.pay.model.alipay.response.AliPayOrderRefundResponse;
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
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author ftm
 * @date 2023/2/15 0015 11:42
 */
@Slf4j
@Component
@Primary
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



//      接下来就是 PC 和 手机WAP支付
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
        requestParams.put("qr_pay_mode", "2");//跳转模式
        requestParams.put("total_amount", String.valueOf(request.getOrderAmount()));
        requestParams.put("subject", String.valueOf(request.getOrderName()));
        requestParams.put("passback_params", request.getAttach());
        aliPayRequest.setAppId(aliPayConfig.getAppId());
        aliPayRequest.setCharset("UTF-8");
        aliPayRequest.setSignType(AliPayConstants.SIGN_TYPE_RSA2);
        aliPayRequest.setNotifyUrl(aliPayConfig.getNotifyUrl());
        //优先使用PayRequest.returnUrl
        aliPayRequest.setReturnUrl(StringUtils.isEmpty(request.getReturnUrl()) ? aliPayConfig.getReturnUrl() : request.getReturnUrl());
        aliPayRequest.setTimestamp(LocalDateTime.now().format(formatter));
        aliPayRequest.setVersion("1.0");
        // 剔除空格、制表符、换行
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
            //      生成form ,暂时这个不使用，如果要用不同类型，建议在配置文件中添加返回类型配置，暂时不需要
            parameters.remove("biz_content");
            String body = WebUtil.buildForm(WebUtil.getRequestUrl(parameters, aliPayConfig.isSandbox()), applicationParams);
            response.setFormContent(body);
        }else{
            response.setPayUrl(baseUrl);
        }
        return response;

    }



    /**
     * 异步通知
     *
     * @param
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
        AliPayConfig aliPayConfig = (AliPayConfig) payConfig;
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


        AliPayResponse payResponse = new AliPayResponse();
        payResponse.setChannelState(ChannelState.WAITING);
        payResponse.setChannelState(AlipayTradeStatusEnum.findByName(tradeStatus).getChannelState());
        if(payResponse.getChannelState() == ChannelState.CONFIRM_FAIL){
            payResponse.setCode(tradeStatus);
        }

        if(payResponse.getChannelState() == ChannelState.CONFIRM_SUCCESS){
            return buildPayResponse(response ,payResponse);
        }
//      其他情况非终态
        return payResponse;
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
     * 查询
     */
    @Override
    public PayResponse query(OrderQueryRequest request,PayConfig payConfig) {
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
            throw new RuntimeException("【查询支付宝订单】网络异常");
        }
        assert retrofitResponse.body() != null;
        AliPayOrderQueryResponse.AlipayTradeQueryResponse response = retrofitResponse.body().getAlipayTradeQueryResponse();
        if (!response.getCode().equals(AliPayConstants.RESPONSE_CODE_SUCCESS)) {
            throw new RuntimeException("【查询支付宝订单】code=" + response.getCode() + ", returnMsg=" + response.getMsg() + String.format("|%s|%s", response.getSubCode(), response.getSubMsg()));
        }


        AliPayResponse aliPayResponse = new AliPayResponse();
        aliPayResponse
                .setChannelState(AlipayTradeStatusEnum.findByName(response.getTradeStatus()).getChannelState());
        aliPayResponse.setOutTradeNo(response.getTradeNo());
                aliPayResponse.setOrderNo(response.getOutTradeNo());
                aliPayResponse.setMsg(response.getMsg());
                aliPayResponse.setFinishTime(response.getSendPayDate());
        return aliPayResponse;
    }

    /**
     * 退款
     *
     * @param request
     * @param payConfig
     */
    @Override
    public RefundResponse refund(RefundRequest request, PayConfig payConfig) {
        AliPayConfig aliPayConfig =(AliPayConfig) payConfig;
        AliPayOrderRefundRequest aliPayOrderRefundRequest = new AliPayOrderRefundRequest();
        aliPayOrderRefundRequest.setAppId(aliPayConfig.getAppId());
        aliPayOrderRefundRequest.setTimestamp(LocalDateTime.now().format(formatter));
        AliPayOrderRefundRequest.BizContent bizContent = new AliPayOrderRefundRequest.BizContent();
        bizContent.setOutTradeNo(request.getOrderNo());
        bizContent.setRefundReason(request.getRefundReason());
        bizContent.setRefundAmount(request.getRefundAmount().doubleValue());
        bizContent.setOutRequestNo(request.getRefundNo());
        aliPayOrderRefundRequest.setBizContent(JsonUtil.toJsonWithUnderscores(bizContent).replaceAll("\\s*", ""));
        aliPayOrderRefundRequest.setSign(AliPaySignature.sign(MapUtil.object2MapWithUnderline(aliPayOrderRefundRequest), aliPayConfig.getPrivateKey()));

        Call<AliPayOrderRefundResponse> call = null;
        if (aliPayConfig.isSandbox()) {
            call = devRetrofit.create(AliPayApi.class).refund((MapUtil.object2MapWithUnderline(aliPayOrderRefundRequest)));
        } else {
            call = retrofit.create(AliPayApi.class).refund((MapUtil.object2MapWithUnderline(aliPayOrderRefundRequest)));
        }

        Response<AliPayOrderRefundResponse> retrofitResponse = null;
        try {
            retrofitResponse = call.execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
        assert retrofitResponse != null;
        if (!retrofitResponse.isSuccessful()) {
            throw new RuntimeException("【支付宝退款】网络异常");
        }
        assert retrofitResponse.body() != null;
        AliPayOrderRefundResponse.AlipayTradeRefundResponse response = retrofitResponse.body().getAlipayTradeRefundResponse();
        if (!response.getCode().equals(AliPayConstants.RESPONSE_CODE_SUCCESS)) {
            throw new RuntimeException("【支付宝退款】code=" + response.getCode() + ", returnMsg=" + response.getMsg() + String.format("|%s|%s", response.getSubCode(), response.getSubMsg()));
        }

        return RefundResponse.builder()
                .outTradeNo(response.getTradeNo())
                .orderNo(response.getOutTradeNo())
                .outRefundNo(response.getTradeNo())
                .refundAmount(response.getRefundFee())
                .refundNo(request.getRefundNo())
                .build();
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
     * @param
     * @param payConfig
     */
    @Override
    public TransferResponse transfer(TransferOrder request, PayConfig payConfig) {
        AliPayConfig aliPayConfig = (AliPayConfig) payConfig;
        AliPayBankRequest aliPayBankRequest = new AliPayBankRequest();
        aliPayBankRequest.setAppId(aliPayConfig.getAppId());
        aliPayBankRequest.setTimestamp(LocalDateTime.now().format(formatter));
        AliPayBankRequest.BizContent bizContent = new AliPayBankRequest.BizContent();
        bizContent.setOutBizNo(request.getTransferId());
        bizContent.setProductCode("TRANS_BANKCARD_NO_PWD"); // 销售产品码。单笔无密转账固定为 TRANS_ACCOUNT_NO_PWD
        bizContent.setOrderTitle("转账");                     // 转账业务的标题，用于在支付宝用户的账单里显示。
        bizContent.setRemark(request.getTransferDesc());
        AliPayBankRequest.BizContent.Participant participant = new AliPayBankRequest.BizContent.Participant();
        participant.setIdentity(request.getAccountNo());
        participant.setName(StringUtils.defaultString(request.getAccountName(), null));
        participant.setIdentityType("BANKCARD_ACCOUNT");  //ALIPAY_USERID： 支付宝用户ID      ALIPAY_LOGONID:支付宝登录账号
        AliPayBankRequest.BizContent.Participant.BankcardExtInfo bankcardExtInfo = new AliPayBankRequest.BizContent.Participant.BankcardExtInfo();
        //暂时先默认对私
        bankcardExtInfo.setAccountType(2);
//        bankcardExtInfo.setBankCode(request.getBankCode());
        participant.setBankcardExtInfo(bankcardExtInfo);
        bizContent.setPayeeInfo(participant);
        aliPayBankRequest.setBizContent(JsonUtil.toJsonWithUnderscores(bizContent).replaceAll("\\s*", ""));
        aliPayBankRequest.setSign(AliPaySignature.sign(MapUtil.object2MapWithUnderline(aliPayBankRequest), aliPayConfig.getPrivateKey()));

        Call<AliPayBankResponse> call = retrofit.create(AliPayApi.class).payBank((MapUtil.object2MapWithUnderline(aliPayBankRequest)));

        Response<AliPayBankResponse> retrofitResponse = null;
        try {
            retrofitResponse = call.execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
        assert retrofitResponse != null;
        if (!retrofitResponse.isSuccessful()) {
            throw new RuntimeException("【支付宝转账到银行卡】网络异常");
        }
        assert retrofitResponse.body() != null;
        AliPayBankResponse.AlipayFundTransUniTransferResponse response = retrofitResponse.body().getAlipayFundTransUniTransferResponse();
        if (!response.getCode().equals(AliPayConstants.RESPONSE_CODE_SUCCESS)) {
            throw new RuntimeException("【支付宝转账到银行卡】code=" + response.getCode() + ", returnMsg=" + response.getMsg());
        }

//        return TransferResponse.builder()
//                .transferId(response.getOutBizNo())
//                .outTradeNo(response.getOrderId())
//                .payFundOrderId(response.getPayRundOrderId())
//                .status(response.getStatus())
//                .build();
        //todo
        return null;
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


    private PayResponse buildPayResponse(AliPayAsyncResponse response,AliPayResponse payResponse) {

//        payResponse.setPayType(PayType.ALIPAY);
        payResponse.setOrderAmount(Double.valueOf(response.getTotalAmount()));
        payResponse.setOrderNo(response.getOutTradeNo());
        payResponse.setOutTradeNo(response.getTradeNo());
        payResponse.setAttach(response.getPassbackParams());
        return payResponse;
    }


}
