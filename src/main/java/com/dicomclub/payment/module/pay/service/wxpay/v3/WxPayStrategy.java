package com.dicomclub.payment.module.pay.service.wxpay.v3;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.dicomclub.payment.common.utils.HttpClientUtil;
import com.dicomclub.payment.common.utils.RequestKitBean;
import com.dicomclub.payment.exception.PayException;
import com.dicomclub.payment.module.pay.common.ChannelStateRes;
import com.dicomclub.payment.module.pay.config.PayConfig;
import com.dicomclub.payment.module.pay.config.WxPayConfig;
import com.dicomclub.payment.module.pay.enums.ChannelState;
import com.dicomclub.payment.module.pay.enums.PayChannel;
import com.dicomclub.payment.module.pay.enums.PayType;
import com.dicomclub.payment.module.pay.model.*;
import com.dicomclub.payment.module.pay.model.wxpay.WxPayRequest;
import com.dicomclub.payment.module.pay.model.wxpay.WxPayResponse;
import com.dicomclub.payment.module.pay.model.wxpay.WxTransferOrder;
import com.dicomclub.payment.module.pay.model.wxpay.WxTransferType;
import com.dicomclub.payment.module.pay.model.wxpay.request.TransferDetail;
import com.dicomclub.payment.module.pay.service.PayStrategy;
import com.dicomclub.payment.module.pay.service.union.common.OrderParaStructure;
import com.dicomclub.payment.module.pay.service.union.common.Util;
import com.dicomclub.payment.module.pay.service.union.common.sign.SignTextUtils;
import com.dicomclub.payment.module.pay.service.union.common.sign.SignUtils;
import com.dicomclub.payment.module.pay.service.union.common.sign.encrypt.RSA2;
import com.dicomclub.payment.module.pay.service.wxpay.WxV2PayStrategy;
import com.dicomclub.payment.module.pay.service.wxpay.v3.common.*;
import com.dicomclub.payment.module.pay.service.wxpay.v3.model.WxPayOrderNotifyV3Result;
import com.dicomclub.payment.module.pay.service.wxpay.v3.model.WxPayRefundNotifyV3Result;
import com.dicomclub.payment.module.pay.service.wxpay.v3.service.WxPayAssistService;
import com.dicomclub.payment.util.DateUtils;
import com.dicomclub.payment.util.httpRequest.*;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.security.cert.Certificate;
import java.util.*;
import java.util.stream.Collectors;

import static com.dicomclub.payment.module.pay.service.wxpay.v3.common.WxConst.OUT_TRADE_NO;

/**
 * @author ftm
 * @date 2023/3/3 0003 10:49
 */
@Slf4j
@Component
@Primary
public class WxPayStrategy extends PayStrategy {
    /**
     * api服务地址，默认为国内
     */
    private String apiServerUrl = WxConst.URI;


    @Autowired
    private HttpRequestTemplate requestTemplate ;


    @Autowired
    private RequestKitBean requestKitBean;


    /**
     * 默认的支付配置
     */
    @Autowired(required = false)
    private WxPayConfig defaulWxPayConfig;



    /**
     * 辅助api
     */
    private WxPayAssistService assistService ;




    public WxPayStrategy(){
        //请求连接池配置
        HttpConfigStorage httpConfigStorage = new HttpConfigStorage();
        //最大连接数
        httpConfigStorage.setMaxTotal(20);
        //默认的每个路由的最大连接数
        httpConfigStorage.setDefaultMaxPerRoute(10);
        requestTemplate = new HttpRequestTemplate(httpConfigStorage);

        if(assistService == null){
            assistService = new WxPayAssistService();
        }
        if(requestKitBean == null){
            requestKitBean= new RequestKitBean();
        }
    }


    public WxPayStrategy(HttpRequestTemplate requestTemplate){
        //请求连接池配置
        HttpConfigStorage httpConfigStorage = new HttpConfigStorage();
        //最大连接数
        httpConfigStorage.setMaxTotal(20);
        //默认的每个路由的最大连接数
        httpConfigStorage.setDefaultMaxPerRoute(10);
        requestTemplate = new HttpRequestTemplate(httpConfigStorage);

        if(assistService == null){
            assistService = new WxPayAssistService();
        }
        if(requestKitBean == null){
            requestKitBean= new RequestKitBean();
        }
    }



    @Override
    public PayResponse pay(PayRequest payRequest, PayConfig payConfig) {
        WxPayRequest request = (WxPayRequest) payRequest;
        if(request.getAuthCode()!=null&&request.getOpenid()==null){
            request.setOpenid(getOpenid(request.getAuthCode(),(WxPayConfig) payConfig,payRequest.getPayChannel() ));
        }
        WxPayConfig payConfigStorage =(WxPayConfig) payConfig;
        ////统一下单
        JSONObject result = unifiedOrder(request,payConfigStorage);
//       交易类型判断
        WxTransactionType wxTransactionType = null;
        PayChannel payChannel = request.getPayChannel();
        if(PayChannel.WXPAY_MINI == payChannel ||PayChannel.WXPAY_MP == payChannel){
            wxTransactionType = WxTransactionType.JSAPI;
        }else if(PayChannel.WXPAY_APP == payChannel){
            wxTransactionType = WxTransactionType.APP;
        }else if(PayChannel.WXPAY_MICRO == payChannel  ){
            wxTransactionType = WxTransactionType.NATIVE;
        }else if(PayChannel.WXPAY_NATIVE == payChannel){
            wxTransactionType = WxTransactionType.NATIVE;
        }else if(PayChannel.WXPAY_MWEB == payChannel){
            wxTransactionType = WxTransactionType.H5;
        }
        WxPayResponse wxPayResponse = new WxPayResponse();

        //如果是扫码支付或者刷卡付无需处理，直接返回
        if (wxTransactionType.isReturn()) {
            wxPayResponse.setDataMap(result);
            return wxPayResponse;
        }
        Map<String, Object> params = new LinkedHashMap<>();
        String appId = payConfigStorage.getAppId();
        if (payConfigStorage.isPartner() && StringUtils.isNotEmpty(payConfigStorage.getSubAppId())) {
            appId = payConfigStorage.getSubAppId();
        }
        String timeStamp = String.valueOf(DateUtils.toEpochSecond());
        String randomStr = SignTextUtils.randomStr();
        String prepayId = result.getString("prepay_id");
        if (WxTransactionType.JSAPI == wxTransactionType) {
            params.put("appId", appId);
            params.put("timeStamp", timeStamp);
            params.put("nonceStr", randomStr);
            prepayId = "prepay_id=" + prepayId;
            params.put("package", prepayId);
            params.put("signType", SignUtils.RSA.getName());
        }
        else if (WxTransactionType.APP ==wxTransactionType) {
            params.put(WxConst.APPID, appId);
            params.put("partnerid", payConfigStorage.getMchId());
            params.put("timestamp", timeStamp);
            params.put("noncestr", randomStr);
            params.put("prepayid", prepayId);
            params.put("package", "Sign=WXPay");
        }
        String signText = StringUtils.joining("\n", appId, timeStamp, randomStr, prepayId);
        String paySign = assistService.createSign(signText, payConfigStorage.getInputCharset(),payConfigStorage);
        params.put(WxTransactionType.JSAPI.equals(wxTransactionType) ? "paySign" : "sign", paySign);
        wxPayResponse.setDataMap(params);
        return wxPayResponse;
    }


    public   RefundResponse refund(RefundRequest refundOrder, PayConfig payConfig){
        WxPayConfig wxPayConfig = (WxPayConfig) payConfig;
        // 微信统一下单请求对象
        JSONObject reqJSON = new JSONObject();
        reqJSON.put("out_trade_no", refundOrder.getOrderNo());   // 订单号
        reqJSON.put("out_refund_no", refundOrder.getRefundNo()); // 退款订单号
        reqJSON.put("notify_url", refundOrder.getNotifyUrl()); // 回调地址
        JSONObject amountJson = new JSONObject();
        amountJson.put("refund", Util.conversionCentAmount(refundOrder.getRefundAmount()));// 退款金额
        amountJson.put("total", Util.conversionCentAmount(refundOrder.getOrderAmount()));// 订单总金额
        amountJson.put("currency", refundOrder.getCurType());// 币种
        reqJSON.put("amount", amountJson);
//      商户信息
        initSubMchId(reqJSON, wxPayConfig);
        JSONObject resultJSON = WxpayV3Util.refundV3(reqJSON);
        String status = resultJSON.getString("status");
        ChannelStateRes channelStateRes = new ChannelStateRes();
        RefundResponse refundResponse = new RefundResponse();
        if("SUCCESS".equals(status)){ // 退款成功
            String refundId = resultJSON.getString("refund_id");
            channelStateRes.setChannelState(ChannelState.CONFIRM_SUCCESS);
            refundResponse.setRefundId(refundId);
        }else if ("PROCESSING".equals(status)){ // 退款处理中
            String refundId = resultJSON.getString("refund_id");
            channelStateRes.setChannelState(ChannelState.WAITING);
            refundResponse.setRefundId(refundId);
        }else{
            channelStateRes.setChannelState(ChannelState.CONFIRM_FAIL);
            channelStateRes.setMsg(status);
        }
        refundResponse.setChannelStateRes(channelStateRes);
        return refundResponse;
    }

    /**
     * 转账
     *
     * @param
     * @param payConfig
     */
    @Override
    public TransferResponse transfer(TransferOrder transferOrder1, PayConfig payConfig) {
        WxTransferOrder transferOrder = (WxTransferOrder) transferOrder1;
        WxPayConfig wxPayConfig = (WxPayConfig) payConfig;
        Map<String, Object> parameters = new HashMap<>(12);
        parameters.put(WxConst.APPID, wxPayConfig.getAppId());
        parameters.put(WxConst.OUT_BATCH_NO, transferOrder.getTransferId());
        parameters.put(WxConst.BATCH_NAME,transferOrder.getAccountName());
        parameters.put(WxConst.BATCH_REMARK, transferOrder.getTransferDesc());
        parameters.put(WxConst.TOTAL_AMOUNT, Util.conversionCentAmount(transferOrder.getAmount()));
        parameters.put(WxConst.TOTAL_NUM, transferOrder.getTotalNum());
        List<TransferDetail> detailList = transferOrder.getTransferDetailList();
        if(detailList == null || detailList.isEmpty()){
            TransferDetail transferDetail = new TransferDetail();
            transferDetail.setOutDetailNo(transferOrder.getTransferId());
            transferDetail.setOpenid(transferOrder.getAccountNo());
            transferDetail.setTransferAmount(Util.conversionCentAmount(transferOrder.getAmount())); //付款金额，单位为分
            transferDetail.setUserName(transferOrder.getAccountName());
            transferDetail.setTransferRemark(transferOrder.getTransferDesc());
            detailList = new ArrayList<>();
            detailList.add(transferDetail);
        }
        List<TransferDetail> transferDetails = initTransferDetailListAttr(detailList,wxPayConfig);
        parameters.put(WxConst.TRANSFER_DETAIL_LIST, transferDetails);
        parameters.put(WxConst.TRANSFER_SCENE_ID, transferOrder.getTransferSceneId());
        JSONObject jsonObject = assistService.doExecute(parameters, WxTransferType.TRANSFER_BATCHES, wxPayConfig);
        Object batch_id = jsonObject.get("batch_id");
        ChannelState channelState = null;
        if(batch_id!=null){
           channelState = ChannelState.CONFIRM_SUCCESS;
        }else{
            channelState = ChannelState.CONFIRM_FAIL;
        }
        return TransferResponse.builder().channelState(channelState).build();
    }




    private List<TransferDetail> initTransferDetailListAttr(Object transferDetailListAttr,WxPayConfig payConfig) {
        List<TransferDetail> transferDetails = null;
        if (transferDetailListAttr instanceof String) {
            transferDetails = JSON.parseArray((String) transferDetailListAttr, TransferDetail.class);
        }
        else if (null != transferDetailListAttr) {
            //偷懒的做法
            transferDetails = JSON.parseArray(JSON.toJSONString(transferDetailListAttr), TransferDetail.class);
        }
        else {
            return null;
        }
        String serialNumber = payConfig.getCertEnvironment().getSerialNumber();
        Certificate certificate = assistService.getCertificate(serialNumber,payConfig);
        return transferDetails.stream()
                .peek(transferDetailListItem -> {
                    String userName = transferDetailListItem.getUserName();
                    if (StringUtils.isNotEmpty(userName)) {
                        String encryptedUserName = AntCertificationUtil.encryptToString(userName, certificate);
                        transferDetailListItem.setUserName(encryptedUserName);
                    }
                    String userIdCard = transferDetailListItem.getUserIdCard();
                    if (StringUtils.isNotEmpty(userIdCard)) {
                        String encryptedUserIdCard = AntCertificationUtil.encryptToString(userIdCard, certificate);
                        transferDetailListItem.setUserIdCard(encryptedUserIdCard);
                    }
                }).collect(Collectors.toList());
    }



    private String getOpenid(String code ,WxPayConfig config,PayChannel payChannel) {
        String urlstr= null;
        if(payChannel == PayChannel.WXPAY_MINI){
            urlstr="https://api.weixin.qq.com/sns/jscode2session?appid="+config.getAppId()+"&secret="+config.getAppSecret()+"&js_code="+code+"&grant_type=authorization_code";

        }else{
            urlstr="https://api.weixin.qq.com/sns/oauth2/access_token?appid="+config.getAppId()+"&secret="+config.getAppSecret()+"&code="+code+"&grant_type=authorization_code";
        }
        System.out.println("获取openid的url："+urlstr);


        String string = HttpClientUtil.doGet(urlstr,new HashMap<>(),"UTF-8");
        //ServletActionContext.getResponse().getWriter().write("getResult:"+string+"|||");
        System.out.println("获得openid"+string);


        Gson gson = new Gson();
        Map map = gson.fromJson(string, Map.class);
        String openid = (String) map.get("openid");
        //测试用
        if(openid==null){
            //ServletActionContext.getResponse().getWriter().write("[openid为空]");
            openid="souaitoysyha";
            throw new PayException("openid获取错误");
        }else{
            //ServletActionContext.getResponse().getWriter().write("[openid不为空]"+ openid);
        }
        return openid;
    }




    /**
     * 微信统一下单接口
     *
     * @param order 支付订单集
     * @return 下单结果
     */
    public JSONObject unifiedOrder(WxPayRequest order ,WxPayConfig payConfig) {
        if(order.getPayChannel() == null){
            throw new PayException("请选择支付渠道");
        }

        //统一下单
        Map<String, Object> parameters =  initPartner(null,payConfig);
//        wxParameterStructure.getPublicParameters(parameters);
        // 商品描述
        OrderParaStructure.loadParameters(parameters, WxConst.DESCRIPTION, order.getOrderName());
        OrderParaStructure.loadParameters(parameters, WxConst.DESCRIPTION, order.getOrderDesc());
        // 订单号
        parameters.put(OUT_TRADE_NO, order.getOrderNo());
        //交易结束时间
        if (null != order.getExpirationTime()) {
            parameters.put("time_expire", DateUtils.formatDate(order.getExpirationTime(), DateUtils.YYYY_MM_DD_T_HH_MM_SS_XX));
        }
        OrderParaStructure.loadParameters(parameters, "attach", order.getAttach());
        initNotifyUrl(parameters, order,payConfig);
        //订单优惠标记
        OrderParaStructure.loadParameters(parameters, "goods_tag", order);
        parameters.put("amount", Amount.getAmount(order.getOrderAmount(), order.getCurType()));

        //优惠功能
        OrderParaStructure.loadParameters(parameters, "detail", order);
        //支付场景描述
        OrderParaStructure.loadParameters(parameters, WxConst.SCENE_INFO, order);
        loadSettleInfo(parameters, order);
        WxTransactionType transactionType = null;
        switch (order.getPayChannel()){
            case WXPAY_MP:
                transactionType = WxTransactionType.JSAPI;
                break;
            case WXPAY_MINI:
                transactionType = WxTransactionType.JSAPI;
                break;
            case WXPAY_APP:
                transactionType = WxTransactionType.APP;
                break;
            case WXPAY_MWEB:
                transactionType = WxTransactionType.H5;
                break;
            case WXPAY_NATIVE:
                transactionType = WxTransactionType.NATIVE;
                break;
            case WXPAY_MICRO:
                transactionType = WxTransactionType.NATIVE;
                break;
            default:
                throw new PayException("错误的渠道");
        }
        transactionType.setAttribute(parameters, order);
        // 订单附加信息，可用于预设未提供的参数，这里会覆盖以上所有的订单信息，
        parameters.putAll(order.getAttrs());
        String requestBody = JSON.toJSONString(parameters);

        return  assistService.doExecute(requestBody,transactionType, payConfig);
    }


    @Override
    public PayResponse query(OrderQueryRequest request, PayConfig payConfig){
        WxPayConfig wxPayConfig = (WxPayConfig) payConfig;
        String transactionId = request.getOrderNo();
        String outTradeNo = request.getOutOrderNo();
        Map<String, Object> attr = new HashMap<>();
        OrderParaStructure.loadParameters(attr, wxPayConfig.isPartner() ? WxConst.SP_MCH_ID : WxConst.MCH_ID, wxPayConfig.getMchId());
        String parameters = UriVariables.getMapToParameters(attr);
        WxTransactionType transactionType = WxTransactionType.QUERY_TRANSACTION_ID;
        String uriVariable = transactionId;
        if (StringUtils.isNotEmpty(outTradeNo)) {
            transactionType = WxTransactionType.QUERY_OUT_TRADE_NO;
            uriVariable = outTradeNo;
        }
        PayResponse build = new WxPayResponse();
        JSONObject resultJSON = assistService.doExecute(parameters, transactionType, wxPayConfig, uriVariable);
        String channelState = resultJSON.getString("trade_state");
        ChannelState channelRes = ChannelState.WAITING;
        if ("SUCCESS".equals(channelState)) {
            channelRes =ChannelState.CONFIRM_SUCCESS;
            String transaction_id = resultJSON.getString("transaction_id");
            build.setOutTradeNo(transaction_id);

        }else if("USERPAYING".equals(channelState)){ //支付中，等待用户输入密码
            //支付中
        }else if("CLOSED".equals(channelState)
                || "REVOKED".equals(channelState)
                || "PAYERROR".equals(channelState)){  //CLOSED—已关闭， REVOKED—已撤销(刷卡支付), PAYERROR--支付失败(其他原因，如银行返回失败)
            channelRes = ChannelState.CONFIRM_FAIL; //支付失败
        }else{
            channelRes = ChannelState.UNKNOWN;
        }
        build.setOrderNo(resultJSON.getString("out_trade_no"));
        build.setMsg(resultJSON.getString("trade_state_desc"));
        build.setFinishTime(resultJSON.getString("success_time"));
        build.setChannelState(channelRes);
        return build;
    }



    @Override
    public PayResponse asyncNotify(HttpServletRequest request, PayConfig payConfig) {
        WxPayConfig wxPayConfig = (WxPayConfig) payConfig;
//        String notifyData = null;
//        try {
//            notifyData = request.getReader().lines().collect(Collectors.joining());
//        } catch (IOException e) {
//            throw new PayException(e.getMessage());
//        }
////  处理字符串参数
//        HashMap<String, Object> dataMap = MapUtil.form2MapObjectAndURLDecode(notifyData);
////  验签
//        if (null == dataMap || dataMap.get(UnionPayConstants.param_signature) == null) {
//            log.debug("微信支付验签异常：params：" + dataMap);
//            throw new PayException("微信验签异常!");
//        }
//      验签解析结果
        String body = verifySignAndGetBody(request, wxPayConfig);
        WxPayOrderNotifyV3Result.DecryptNotifyResult result =assistService.parseOrderNotifyV3Result(body, wxPayConfig).getResult();

        PayResponse response = new WxPayResponse();
        String channelState = result.getTradeState();
        if ("SUCCESS".equals(channelState)) {
            response.setChannelState(ChannelState.CONFIRM_SUCCESS);
        }else if("CLOSED".equals(channelState)
                || "REVOKED".equals(channelState)
                || "PAYERROR".equals(channelState)){  //CLOSED—已关闭， REVOKED—已撤销, PAYERROR--支付失败
            response.setChannelState(ChannelState.CONFIRM_FAIL); //支付失败
        }

        response.setOutTradeNo(result.getTransactionId()); //渠道订单号
        WxPayOrderNotifyV3Result.Payer payer = result.getPayer();
        if (payer != null) {
//            refundResponse.setChannelUserId(payer.getOpenid()); //支付用户ID
        }
        response.setFinishTime(result.getSuccessTime());
        return response;
    }





    @Override
    public RefundResponse asyncNotifyRefund(HttpServletRequest request, PayConfig payConfig) {
        WxPayConfig wxPayConfig = (WxPayConfig) payConfig;
//      验签解析结果
        String body = verifySignAndGetBody(request, wxPayConfig);
        WxPayRefundNotifyV3Result.DecryptNotifyResult notifyResult = assistService.parseRefundNotifyV3Result(body,wxPayConfig);
        RefundResponse result = new RefundResponse();
        String refundStatus = notifyResult.getRefundStatus();
        ChannelStateRes channelStateRes = new ChannelStateRes();
        if ("SUCCESS".equals(refundStatus)) {
            channelStateRes.setChannelState(ChannelState.CONFIRM_SUCCESS);
        }else {  //CHANGE—退款异常， REFUNDCLOSE—退款关闭
            channelStateRes.setChannelState(ChannelState.CONFIRM_FAIL); //退款失败
        }
        result.setOutRefundNo(notifyResult.getTransactionId()); // 渠道订单号
        result.setFinishTime(notifyResult.getSuccessTime());
        result.setPayType(PayType.WX);
        return result;
    }







    /**
     * 退款查询
     *
     * @param payConfig
     */
    @Override
    public RefundResponse refundQuery(RefundQueryRequest refundQueryRequest, PayConfig payConfig) {
        String refundNo = refundQueryRequest.getRefundNo();
        WxPayConfig wxPayConfig = (WxPayConfig) payConfig;
        JSONObject resultJSON = null;
        if ( wxPayConfig.isPartner()) {
            resultJSON = WxpayV3Util.refundQueryV3Isv(refundNo,  wxPayConfig.getSubMchId());
        }else {
            resultJSON = WxpayV3Util.refundQueryV3(refundNo);
        }
        String status = resultJSON.getString("status");
        ChannelStateRes channelStateRes = new ChannelStateRes();
        channelStateRes.setData(resultJSON);
        if("SUCCESS".equals(status)){ // 退款成功
            channelStateRes.setChannelState(ChannelState.CONFIRM_SUCCESS);
        }else{
            channelStateRes.setChannelState(ChannelState.WAITING);
            channelStateRes.setMsg(status);
        }
        RefundResponse refundResponse = new RefundResponse();
        refundResponse.setChannelStateRes(channelStateRes);
        return refundResponse;
    }


    private String verifySignAndGetBody(HttpServletRequest request, WxPayConfig wxPayConfig) {
        //当前使用的微信平台证书序列号
        String serial = request.getHeader("wechatpay-serial");
        //微信服务器的时间戳
        String timestamp = request.getHeader("wechatpay-timestamp");
        //微信服务器提供的随机串
        String nonce = request.getHeader("wechatpay-nonce");
        //微信平台签名
        String signature = request.getHeader("wechatpay-signature");

        Certificate certificate = assistService.getCertificate(serial,wxPayConfig);
        //这里为微信回调时的请求内容体，原值数据
        String body =requestKitBean.getReqParamFromBody();
        //签名信息
        String signText = StringUtils.joining("\n", timestamp, nonce, body);

        boolean verify = RSA2.verify(signText, signature, certificate, wxPayConfig.getInputCharset());
        if(!verify){
            throw new PayException("微信验签失败!");
        }
        return body;
    }


    /**
     * 初始化商户相关信息
     *
     * @param parameters 参数信息
     * @return 参数信息
     */
    public Map<String, Object> initPartner(Map<String, Object> parameters, WxPayConfig payConfigStorage) {
        if (null == parameters) {
            parameters = new LinkedHashMap<>();
        }
        if (payConfigStorage.isPartner()) {
            parameters.put("sp_appid", payConfigStorage.getAppId());
            parameters.put(WxConst.SP_MCH_ID, payConfigStorage.getMchId());
            OrderParaStructure.loadParameters(parameters, "sub_appid", payConfigStorage.getSubAppId());
            OrderParaStructure.loadParameters(parameters, WxConst.SUB_MCH_ID, payConfigStorage.getSubMchId());
            return parameters;
        }
        parameters.put(WxConst.APPID, payConfigStorage.getAppId());
        parameters.put(WxConst.MCH_ID, payConfigStorage.getMchId());
        return parameters;
    }

    /**
     * 初始化商户相关信息
     *
     * @param parameters 参数信息
     * @return 参数信息
     */
    public Map<String, Object> initSubMchId(Map<String, Object> parameters, WxPayConfig payConfigStorage) {
        if (null == parameters) {
            parameters = new HashMap<>();
        }
        if (payConfigStorage.isPartner()) {
            OrderParaStructure.loadParameters(parameters, WxConst.SUB_MCH_ID, payConfigStorage.getSubMchId());
        }

        return parameters;

    }


    /**
     * 初始化通知URL必须为直接可访问的URL，不允许携带查询串，要求必须为https地址。
     *
     * @param parameters 订单参数
     * @param order      订单信息
     */
    public void initNotifyUrl(Map<String, Object> parameters,AssistOrder order,WxPayConfig wxPayConfig) {
        OrderParaStructure.loadParameters(parameters, WxConst.NOTIFY_URL, wxPayConfig.getNotifyUrl());
        OrderParaStructure.loadParameters(parameters, WxConst.NOTIFY_URL, order.getNotifyUrl());
        OrderParaStructure.loadParameters(parameters, WxConst.NOTIFY_URL, order);
    }


    /**
     * 加载结算信息
     *
     * @param parameters 订单参数
     * @param order      支付订单
     */
    public void loadSettleInfo(Map<String, Object> parameters, PayRequest order) {
        Object profitSharing = order.getAttr("profit_sharing");
        if (null != profitSharing) {
            Map<String, Object> settleInfo = new MapGen<>("profit_sharing", profitSharing).getAttr();
            parameters.put("settle_info", settleInfo);
            return;
        }
        //结算信息
        OrderParaStructure.loadParameters(parameters, "settle_info", order);
    }



}


