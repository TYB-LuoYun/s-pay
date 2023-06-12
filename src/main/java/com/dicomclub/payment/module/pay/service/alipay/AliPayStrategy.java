package com.dicomclub.payment.module.pay.service.alipay;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alipay.api.domain.AlipayTradeOrderSettleModel;
import com.alipay.api.domain.OpenApiRoyaltyDetailInfoPojo;
import com.alipay.api.domain.SettleExtendParams;
import com.alipay.api.response.AlipayTradeOrderSettleResponse;
import com.dicomclub.payment.exception.PayException;
import com.dicomclub.payment.module.pay.common.ChannelStateRes;
import com.dicomclub.payment.module.pay.common.TransactionType;
import com.dicomclub.payment.module.pay.config.AliPayConfig;
import com.dicomclub.payment.module.pay.config.PayConfig;
import com.dicomclub.payment.module.pay.constants.AliPayConstants;
import com.dicomclub.payment.module.pay.enums.*;
import com.dicomclub.payment.module.pay.model.*;
import com.dicomclub.payment.module.pay.model.alipay.AliPayApi;
import com.dicomclub.payment.module.pay.model.alipay.AliPayRequest;
import com.dicomclub.payment.module.pay.model.alipay.AliPayResponse;
import com.dicomclub.payment.module.pay.model.alipay.CertEnvironment;
import com.dicomclub.payment.module.pay.model.alipay.request.AliPayBankRequest;
import com.dicomclub.payment.module.pay.model.alipay.request.AliPayOrderQueryRequest;
import com.dicomclub.payment.module.pay.model.alipay.request.AliPayOrderRefundRequest;
import com.dicomclub.payment.module.pay.model.alipay.request.AliPayPcRequest;
import com.dicomclub.payment.module.pay.model.alipay.response.AliPayAsyncResponse;
import com.dicomclub.payment.module.pay.model.alipay.response.AliPayBankResponse;
import com.dicomclub.payment.module.pay.model.alipay.response.AliPayOrderQueryResponse;
import com.dicomclub.payment.module.pay.model.alipay.response.AliPayOrderRefundResponse;
import com.dicomclub.payment.module.pay.model.wxpay.BillResponse;
import com.dicomclub.payment.module.pay.model.wxpay.DivisionReceiver;
import com.dicomclub.payment.module.pay.model.wxpay.DivisionReceiverBind;
import com.dicomclub.payment.module.pay.model.wxpay.BillRequest;
import com.dicomclub.payment.module.pay.service.PayStrategy;
import com.dicomclub.payment.module.pay.service.alipay.channel.AlipayAppService;
import com.dicomclub.payment.module.pay.service.alipay.channel.AlipayBarCodeService;
import com.dicomclub.payment.module.pay.service.alipay.channel.AlipayH5Service;
import com.dicomclub.payment.module.pay.service.alipay.channel.AlipayQRCodeService;
import com.dicomclub.payment.module.pay.service.alipay.common.AliPaySignature;
import com.dicomclub.payment.module.pay.service.alipay.common.AliTransactionType;
import com.dicomclub.payment.module.pay.service.alipay.common.RegKit;
import com.dicomclub.payment.module.pay.service.union.common.OrderParaStructure;
import com.dicomclub.payment.module.pay.service.union.common.sign.SignTextUtils;
import com.dicomclub.payment.module.pay.service.union.common.sign.SignUtils;
import com.dicomclub.payment.module.pay.service.wxpay.v3.service.WxPayAssistService;
import com.dicomclub.payment.util.*;
import com.dicomclub.payment.util.httpRequest.HttpRequestTemplate;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.GsonBuilder;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLDecoder;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author ftm
 * @date 2023/2/15 0015 11:42
 */
@Slf4j
@Component
@Primary
public class AliPayStrategy extends PayStrategy {

    /**
     * 正式测试环境
     */
    public static final String HTTPS_REQ_URL = "https://openapi.alipay.com/gateway.do";
    /**
     * 沙箱测试环境账号
     */
    public static final String DEV_REQ_URL = "https://openapi-sandbox.dl.alipaydev.com/gateway.do";

    protected final static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired
    private AlipayH5Service alipayH5Service;

    @Autowired
    private AlipayQRCodeService alipayQRCodeService;

    @Autowired
    private AlipayBarCodeService alipayBarCodeService;

    @Autowired
    private AlipayAppService alipayAppService;

    @Autowired
    private HttpRequestTemplate requestTemplate;

    /**
     * 辅助api
     */
    private WxPayAssistService assistService ;



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
//        requestParams.put("qr_pay_mode", "2");//跳转模式
        requestParams.put("total_amount", String.valueOf(request.getOrderAmount()));
        requestParams.put("subject", String.valueOf(request.getOrderName()));
        requestParams.put("passback_params", request.getAttach());
        aliPayRequest.setAppId(aliPayConfig.getAppId());
        aliPayRequest.setCharset("UTF-8");
        aliPayRequest.setSignType(payConfig.getSignType()==null?AliPayConstants.SIGN_TYPE_RSA2:payConfig.getSignType());
        aliPayRequest.setNotifyUrl(aliPayConfig.getNotifyUrl());
        //优先使用PayRequest.returnUrl
        aliPayRequest.setReturnUrl(StringUtils.isEmpty(request.getReturnUrl()) ? aliPayConfig.getReturnUrl() : request.getReturnUrl());
        aliPayRequest.setTimestamp(LocalDateTime.now().format(formatter));
        aliPayRequest.setVersion("1.0");
        // 剔除空格、制表符、换行
        aliPayRequest.setBizContent(JsonUtil.toJson(requestParams).replaceAll("\\s*", ""));
//        aliPayRequest.setAlipaySdk( "alipay-sdk-java-dynamicVersionNo");
        aliPayRequest.setFormat("json");
        Map<String, String> map = MapUtil.object2MapWithUnderline(aliPayRequest);
        loadCert(map,aliPayConfig );
        map.put("sign",AliPaySignature.sign(map, aliPayConfig.getPrivateKey()) );


        Map<String, String> applicationParams = new HashMap<>();
        applicationParams.put("biz_content", aliPayRequest.getBizContent());
//        parameters.remove("biz_content");
        String baseUrl = WebUtil.getRequestUrl(map, aliPayConfig.isSandbox());
        AliPayResponse response = new AliPayResponse();
        if(request.getPayDataType()!=null&&request.getPayDataType() == PayDataType.FORM){
            //      生成form ,暂时这个不使用，如果要用不同类型，建议在配置文件中添加返回类型配置，暂时不需要
            map.remove("biz_content");
            String body = WebUtil.buildForm(WebUtil.getRequestUrl(map, aliPayConfig.isSandbox()), applicationParams);
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
        if (!verify(MapUtil.form2MapObject(notifyData), aliPayConfig )) {
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

    private boolean verify(HashMap<String, Object> params, AliPayConfig aliPayConfig) {
        if(params.get("sign") == null){
            log.debug("支付宝支付异常：params：{}", params);
            return false;
        }
        return signVerify(params,  (String) params.get("sign"),aliPayConfig);
    }

    private boolean signVerify(Map<String,Object>  params, String sign,AliPayConfig payConfigStorage) {
        if (params instanceof JSONObject) {
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                if (AliPayConstants.SIGN.equals(entry.getKey()) || AliPayConstants.ALIPAY_CERT_SN_FIELD.equals(entry.getKey())) {
                    continue;
                }
                TreeMap<String, Object> response = new TreeMap((Map<String, Object>) entry.getValue());
                LinkedHashMap<Object, Object> linkedHashMap = new LinkedHashMap<>();
                linkedHashMap.put("code", response.remove("code"));
                linkedHashMap.put("msg", response.remove("msg"));
                linkedHashMap.putAll(response);
                return SignUtils.valueOf(payConfigStorage.getSignType()).verify(JSON.toJSONString(linkedHashMap), sign, getKeyPublic(params,payConfigStorage), payConfigStorage.getInputCharset());
            }
        }
        return SignUtils.valueOf(payConfigStorage.getSignType()).verify(params, sign, getKeyPublic(params,payConfigStorage), payConfigStorage.getInputCharset());

    }

    /**
     * 获取公钥信息
     *
     * @param params 响应参数
     * @return 公钥信息
     */
    protected String getKeyPublic(Map<String, Object> params,AliPayConfig payConfigStorage) {
        if (!payConfigStorage.getUseCert()) {
            return payConfigStorage.getAliPayPublicKey();
        }
        return payConfigStorage.getCertEnvironment().getAliPayPublicKey(getAliPayCertSN(params));
    }

    /**
     * 从响应Map中提取支付宝公钥证书序列号
     *
     * @param respMap 响应Map
     * @return 支付宝公钥证书序列号
     */
    public String getAliPayCertSN(java.util.Map<String, Object> respMap) {
        return (String) respMap.get(AliPayConstants.ALIPAY_CERT_SN_FIELD);
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
        if(StringUtils.isNotBlank(payConfig.getSignType())){
            aliPayOrderQueryRequest.setSignType(payConfig.getSignType());
        }
        aliPayOrderQueryRequest.setBizContent(JsonUtil.toJsonWithUnderscores(bizContent).replaceAll("\\s*", ""));
        Map<String, String> map = MapUtil.object2MapWithUnderline(aliPayOrderQueryRequest);
        loadCert(map,aliPayConfig );
        map.put("sign",AliPaySignature.sign(map, aliPayConfig.getPrivateKey()) );

        Call<AliPayOrderQueryResponse> call = null;
        if (aliPayConfig.isSandbox()) {
            call = devRetrofit.create(AliPayApi.class).orderQuery(map);
        } else {
            call = retrofit.create(AliPayApi.class).orderQuery(map);
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

        AliPayResponse aliPayResponse = new AliPayResponse();
        AliPayOrderQueryResponse.AlipayTradeQueryResponse response = retrofitResponse.body().getAlipayTradeQueryResponse();
        if (!response.getCode().equals(AliPayConstants.RESPONSE_CODE_SUCCESS)) {
           aliPayResponse.setChannelState(ChannelState.UNKNOWN);
            aliPayResponse.setMsg(response.getMsg()+response.getSubMsg());
//            throw new RuntimeException("【查询支付宝订单】code=" + response.getCode() + ", returnMsg=" + response.getMsg() + String.format("|%s|%s", response.getSubCode(), response.getSubMsg()));
//            aliPayResponse
//                    .setChannelState(Chan);
//            aliPayResponse.setOrderNo(response.getOutTradeNo());
//            aliPayResponse.setMsg(response.getMsg()+response.getMsg()==null?"":"-"+response.getSubMsg());
//            aliPayResponse.setCode(response.getCode()+response.getSubCode()==null?"":"-"+response.getSubCode());
        }else{

            aliPayResponse
                    .setChannelState(AlipayTradeStatusEnum.findByName(response.getTradeStatus()).getChannelState());
            aliPayResponse.setOutTradeNo(response.getTradeNo());
            aliPayResponse.setOrderNo(response.getOutTradeNo());
            aliPayResponse.setMsg(response.getMsg());
            aliPayResponse.setFinishTime(response.getSendPayDate());
            aliPayResponse.setPayerAccount(response.getBuyerLogonId());
            aliPayResponse.setPayerAccountType("accountName");
        }
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
        if(StringUtils.isNotBlank(payConfig.getSignType())){
            aliPayOrderRefundRequest.setSignType(payConfig.getSignType());
        }
        aliPayOrderRefundRequest.setAppId(aliPayConfig.getAppId());
        aliPayOrderRefundRequest.setTimestamp(LocalDateTime.now().format(formatter));
        AliPayOrderRefundRequest.BizContent bizContent = new AliPayOrderRefundRequest.BizContent();
        bizContent.setOutTradeNo(request.getOrderNo());
        bizContent.setRefundReason(request.getRefundReason());
        bizContent.setRefundAmount(request.getRefundAmount().doubleValue());
        bizContent.setOutRequestNo(request.getRefundNo());
        aliPayOrderRefundRequest.setBizContent(JsonUtil.toJsonWithUnderscores(bizContent).replaceAll("\\s*", ""));
        Map<String, String> map = MapUtil.object2MapWithUnderline(aliPayOrderRefundRequest);
        loadCert(map,aliPayConfig );
        map.put("sign",AliPaySignature.sign(map, aliPayConfig.getPrivateKey()) );

        Call<AliPayOrderRefundResponse> call = null;
        if (aliPayConfig.isSandbox()) {
            call = devRetrofit.create(AliPayApi.class).refund(map);
        } else {
            call = retrofit.create(AliPayApi.class).refund(map);
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
        if(StringUtils.isNotBlank(payConfig.getSignType())){
            aliPayBankRequest.setSignType(payConfig.getSignType());
        }
        aliPayBankRequest.setAppId(aliPayConfig.getAppId());
        aliPayBankRequest.setTimestamp(LocalDateTime.now().format(formatter));
        AliPayBankRequest.BizContent bizContent = new AliPayBankRequest.BizContent();
        bizContent.setOutBizNo(request.getTransferNo());
        bizContent.setProductCode("TRANS_BANKCARD_NO_PWD"); // 销售产品码。单笔无密转账固定为 TRANS_ACCOUNT_NO_PWD
        bizContent.setOrderTitle("转账");                     // 转账业务的标题，用于在支付宝用户的账单里显示。
        bizContent.setRemark(request.getRemark());
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

        Map<String, String> map = MapUtil.object2MapWithUnderline(aliPayBankRequest);
        loadCert(map,aliPayConfig );
        map.put("sign",AliPaySignature.sign(map, aliPayConfig.getPrivateKey()) );

        Call<AliPayBankResponse> call = retrofit.create(AliPayApi.class).payBank(map);

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

    @Override
    public TransferResponse transferQuery(TransferQueryRequest request, PayConfig payConfig) {
        return null;
    }

    /**
     * 分账
     * @param payConfig
     */
    @Override
    public DivisionResponse division(DivisionRquest divisionRquest, PayConfig payConfig) {
        AliPayConfig aliPayConfig = (AliPayConfig) payConfig;
        List<DivisionReceiver>recordList = divisionRquest.getReceivers();
        if(recordList == null || recordList.isEmpty()){ // 当无分账用户时， 支付宝不允许发起分账请求， 支付宝没有完结接口，直接响应成功即可。
            return DivisionResponse.builder().channelStateRes(ChannelStateRes.builder().channelState(ChannelState.CONFIRM_SUCCESS).build()).build();
        }

 

        //获取公共参数
        Map<String, Object> request = getPublicParameters(aliPayConfig,AliTransactionType.DIVISION); 
        
        AlipayTradeOrderSettleModel model = new AlipayTradeOrderSettleModel();

        model.setOutRequestNo(divisionRquest.getDivisionBatchNo()); //结算请求流水号，由商家自定义。32个字符以内，仅可包含字母、数字、下划线。需保证在商户端不重复。
        model.setTradeNo(divisionRquest.getOutTradeNo()); //支付宝订单号


        if(aliPayConfig.isPartner()){
            //统一放置 isv接口必传信息
            // TODO: 2023/4/17 0017
//        AlipayKit.putApiIsvInfo(mchAppConfigContext, request, model);
        }

        List<OpenApiRoyaltyDetailInfoPojo> reqReceiverList = new ArrayList<>();

        for (int i = 0; i < recordList.size(); i++) {
            DivisionReceiver record = recordList.get(i);

            if(record.getDivisionAmount().compareTo(BigDecimal.ZERO) <=0){ //金额为 0 不参与分账处理
                continue;
            }

            OpenApiRoyaltyDetailInfoPojo reqReceiver = new OpenApiRoyaltyDetailInfoPojo();
            reqReceiver.setRoyaltyType("transfer"); //分账类型： 普通分账

            // 出款信息
            // reqReceiver.setTransOutType("loginName"); reqReceiver.setTransOut("xqxemt4735@sandbox.com");

            // 入款信息
            reqReceiver.setTransIn(record.getAccountNo()); //收入方账号
            reqReceiver.setTransInType("loginName");
            if(RegKit.isAlipayUserId(record.getAccountNo())){
                reqReceiver.setTransInType("userId");
            }
            // 分账金额
            reqReceiver.setAmount(record.getDivisionAmount().toString());
            reqReceiver.setDesc("[" + divisionRquest.getOrderNo() + "]订单分账");
            reqReceiverList.add(reqReceiver);

        }

        if(reqReceiverList.isEmpty()){ // 当无分账用户时， 支付宝不允许发起分账请求， 支付宝没有完结接口，直接响应成功即可。
            return DivisionResponse.builder().channelStateRes(ChannelStateRes.builder().channelState(ChannelState.CONFIRM_SUCCESS).build()).build();
        }

        model.setRoyaltyParameters(reqReceiverList); // 分账明细信息

        // 完结
        SettleExtendParams settleExtendParams = new SettleExtendParams();
        settleExtendParams.setRoyaltyFinish("true");
        model.setExtendParams(settleExtendParams);


        request.put("biz_content", JSON.toJSONString(model));

         //设置签名
        setSign(request,aliPayConfig);

        //调起支付宝分账接口
        if(log.isInfoEnabled()){
            log.info("订单：[{}], 支付宝分账请求：{}", divisionRquest.getOrderNo(), JSON.toJSONString(model));
        }
        AlipayTradeOrderSettleResponse alipayResp = requestTemplate.postForObject(getReqUrl(AliTransactionType.DIVISION,aliPayConfig), request, AlipayTradeOrderSettleResponse.class);

        log.info("订单：[{}], 支付宝分账响应：{}", divisionRquest.getOrderNo(), alipayResp.getBody());
        DivisionResponse divisionResponse = new DivisionResponse();

        ChannelState channelState =null;
        ChannelStateRes channelStateRes = new ChannelStateRes();
        if(alipayResp.isSuccess()){
            channelState =ChannelState.CONFIRM_SUCCESS;
            divisionResponse.setOutDivisionBatchNo(alipayResp.getTradeNo());
        }else{
            channelState =ChannelState.CONFIRM_FAIL;
            channelStateRes.setMsg(alipayResp.getMsg()+alipayResp.getSubMsg());
            channelStateRes.setCode(alipayResp.getCode()+alipayResp.getSubCode());
        }
        channelStateRes.setChannelState(channelState);
        //异常：
        return divisionResponse;
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

    /**
     * 生成并设置签名
     *
     * @param parameters 请求参数
     * @return 请求参数
     */
    protected Map<String, Object> setSign(Map<String, Object> parameters,AliPayConfig aliPayConfig) {
        parameters.put("sign_type", aliPayConfig.getSignType());
        String sign = createSign(SignTextUtils.parameterText(parameters, "&", "sign"), aliPayConfig.getInputCharset(),aliPayConfig);
        parameters.put("sign", sign);
        return parameters;
    }

    public String createSign(String content, String characterEncoding,AliPayConfig aliPayConfig) {
        return SignUtils.valueOf(aliPayConfig.getSignType()).createSign(content,aliPayConfig.getPrivateKey(), characterEncoding);
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


    /**
     * 获取公共请求参数
     *
     * @return 放回公共请求参数
     */
    protected Map<String, Object> getPublicParameters(AliPayConfig payConfig, TransactionType transactionType) {
        boolean depositBack = transactionType == AliTransactionType.REFUND_DEPOSITBACK_COMPLETED;
//        boolean depositBack = false;
        Map<String, Object> orderInfo = new TreeMap<>();
        orderInfo.put("app_id", payConfig.getAppId());
        orderInfo.put("charset", payConfig.getInputCharset());
        String method = "method";
        String version = "1.0";
        if (depositBack) {
            method = "msg_method";
            orderInfo.put("utc_timestamp", System.currentTimeMillis());
            version = "1.1";
        }
        else {
            orderInfo.put("timestamp", DateUtils.format(new Date()));
        }

        orderInfo.put(method, transactionType.getMethod());
        orderInfo.put("version", version);

        loadCertSn(orderInfo,payConfig);
        return orderInfo;
    }

    /**
     * 加载证书序列
     *
     * @param orderInfo 订单信息
     */
    protected void loadCertSn(Map<String, Object> orderInfo,AliPayConfig payConfigStorage) {
        if (payConfigStorage.getUseCert()) {
            final CertEnvironment certEnvironment = payConfigStorage.getCertEnvironment();
            OrderParaStructure.loadParameters(orderInfo, "app_cert_sn", certEnvironment.getMerchantCertSN());
            OrderParaStructure.loadParameters(orderInfo, "alipay_root_cert_sn", certEnvironment.getRootCertSN());
        }
    }

    public void loadCert(Map<String, String> orderInfo,AliPayConfig payConfigStorage) {
        if (payConfigStorage.getUseCert()!=null && payConfigStorage.getUseCert() == true) {
            final CertEnvironment certEnvironment = payConfigStorage.getCertEnvironment();
            if(StringUtils.isNotBlank(certEnvironment.getMerchantCertSN())){
                orderInfo.put("app_cert_sn", certEnvironment.getMerchantCertSN());
            }
            if(StringUtils.isNotBlank(certEnvironment.getRootCertSN())){
                orderInfo.put("alipay_root_cert_sn", certEnvironment.getRootCertSN());
            }
        }
    }

    /**
     * 获取对应的请求地址
     *https://openapi-sandbox.dl.alipaydev.com/gateway.do
     * @return 请求地址
     */
    public String getReqUrl(TransactionType transactionType,AliPayConfig payConfig) {
        return payConfig.isSandbox() ?  DEV_REQ_URL : HTTPS_REQ_URL + transactionType.getMethod();
    }





}
