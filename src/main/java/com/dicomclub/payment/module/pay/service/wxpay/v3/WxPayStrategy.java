package com.dicomclub.payment.module.pay.service.wxpay.v3;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.dicomclub.payment.common.utils.HttpClientUtil;
import com.dicomclub.payment.common.utils.RequestKitBean;
import com.dicomclub.payment.exception.PayErrorException;
import com.dicomclub.payment.exception.PayException;
import com.dicomclub.payment.exception.WxPayError;
import com.dicomclub.payment.module.pay.common.AesUtils;
import com.dicomclub.payment.module.pay.common.ChannelStateRes;
import com.dicomclub.payment.module.pay.common.TransactionType;
import com.dicomclub.payment.module.pay.config.PayConfig;
import com.dicomclub.payment.module.pay.config.WxPayConfig;
import com.dicomclub.payment.module.pay.enums.*;
import com.dicomclub.payment.module.pay.model.*;
import com.dicomclub.payment.module.pay.model.common.BillType;
import com.dicomclub.payment.module.pay.model.wxpay.*;
import com.dicomclub.payment.module.pay.model.wxpay.request.WxTransferDetail;
import com.dicomclub.payment.module.pay.service.PayStrategy;
import com.dicomclub.payment.module.pay.service.huifu.model.FundUserOpenReq;
import com.dicomclub.payment.module.pay.service.huifu.model.FundUserOpenRes;
import com.dicomclub.payment.module.pay.service.union.common.OrderParaStructure;
import com.dicomclub.payment.module.pay.service.union.common.Util;
import com.dicomclub.payment.module.pay.service.union.common.sign.SignTextUtils;
import com.dicomclub.payment.module.pay.service.union.common.sign.SignUtils;
import com.dicomclub.payment.module.pay.service.union.common.sign.encrypt.RSA2;
import com.dicomclub.payment.module.pay.service.wxpay.v3.common.*;
import com.dicomclub.payment.module.pay.service.wxpay.v3.model.WxAccountType;
import com.dicomclub.payment.module.pay.service.wxpay.v3.model.WxPayOrderNotifyV3Result;
import com.dicomclub.payment.module.pay.service.wxpay.v3.model.WxPayRefundNotifyV3Result;
import com.dicomclub.payment.module.pay.service.wxpay.v3.service.WxPayAssistService;
import com.dicomclub.payment.util.DateUtils;
import com.dicomclub.payment.util.JsonUtil;
import com.dicomclub.payment.util.MoneyUtil;
import com.dicomclub.payment.util.httpRequest.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
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
        JSONObject  resultJSON  = assistService.doExecute(JSON.toJSONString(reqJSON), WxTransactionType.REFUND, wxPayConfig);
        String status = resultJSON.getString("status");
        ChannelStateRes channelStateRes = new ChannelStateRes();
        RefundResponse refundResponse = new RefundResponse();
        if("SUCCESS".equals(status)){ // 退款成功
            String refundId = resultJSON.getString("refund_id");
            channelStateRes.setChannelState(ChannelState.CONFIRM_SUCCESS);
            refundResponse.setOutRefundNo(refundId);
        }else if ("PROCESSING".equals(status)){ // 退款处理中
            String refundId = resultJSON.getString("refund_id");
            channelStateRes.setChannelState(ChannelState.WAITING);
            refundResponse.setOutRefundNo(refundId);
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
    public TransferResponse transferBatch(TransferOrderBatch transferOrder, PayConfig payConfig) {
        WxPayConfig wxPayConfig = (WxPayConfig) payConfig;
        Map<String, Object> parameters = new HashMap<>(12);
        parameters.put(WxConst.APPID, wxPayConfig.getAppId());
        parameters.put(WxConst.OUT_BATCH_NO, transferOrder.getTransferBatchNo());
        parameters.put(WxConst.BATCH_NAME,transferOrder.getBatchName());
        parameters.put(WxConst.BATCH_REMARK, transferOrder.getRemark());
        parameters.put(WxConst.TOTAL_AMOUNT, Util.conversionCentAmount(transferOrder.getTotalAmount()));
        parameters.put(WxConst.TOTAL_NUM, transferOrder.getTotalNum());
        List<WxTransferDetail> wxDetailList = new ArrayList<>();
        List<TransferOrder> detailList = transferOrder.getTransferDetails();
        if(detailList == null || detailList.isEmpty()){
            throw new PayException("未填转账详情");
        }else{
            wxDetailList = detailList.stream().map(e->{
                WxTransferDetail detail = new WxTransferDetail();
                detail.setOpenid(e.getAccountNo());
                detail.setOutDetailNo(e.getTransferNo());
                detail.setTransferAmount(Util.conversionCentAmount(e.getAmount()));
                detail.setTransferRemark(e.getRemark());
                detail.setUserName(e.getAccountName());
                return detail;
            }).collect(Collectors.toList());
        }
        List<WxTransferDetail> transferDetails = initTransferDetailListAttr(wxDetailList,wxPayConfig);
        parameters.put(WxConst.TRANSFER_DETAIL_LIST, transferDetails);
//        https://pay.weixin.qq.com/wiki/doc/apiv3/apis/chapter4_3_1.shtml
//        parameters.put(WxConst.TRANSFER_SCENE_ID, transferOrder.getTransferSceneId());
        JSONObject jsonObject = assistService.doExecute(JsonUtil.toJsonCustom(parameters), WxTransferType.TRANSFER_BATCHES, wxPayConfig);
        Object batch_id = jsonObject.get("batch_id");
        ChannelState channelState = null;
        if(batch_id!=null){
           channelState = ChannelState.CONFIRM_SUCCESS;
        }else{
            channelState = ChannelState.CONFIRM_FAIL;
        }
        ChannelStateRes build = ChannelStateRes.builder().channelState(channelState).build();
        return TransferResponse.builder().channelStateRes(build).build();
    }


    @Override
    public DivisionResponse divisionQuery(DivisionQueryRquest divisionQueryRquest, PayConfig payConfig){
        WxPayConfig wxPayConfig = (WxPayConfig) payConfig;
        Map<String, Object> map = initSubMchId(null, wxPayConfig);
        map.put("transaction_id", divisionQueryRquest.getOutTradeNo());
        String parameters = UriVariables.getMapToParameters(map);
        JSONObject resultJSON  =  assistService.doExecute(parameters, WxTransactionType.DIVISION_QUERY,wxPayConfig, divisionQueryRquest.getDivisionBatchNo());
        return processDivisionResult(resultJSON,null);
    }
    /**
     * 分账
     *
     */
    @Override
    public DivisionResponse division(DivisionRquest divisionRquest, PayConfig payConfig) {
        ProfitSharingRequest request = new ProfitSharingRequest();
        request.setTransactionId(divisionRquest.getOutTradeNo());
        WxPayConfig wxPayConfig = (WxPayConfig) payConfig;
        request.setAppid(wxPayConfig.getAppId());
        // 特约商户
        if(wxPayConfig.isPartner()){
            request.setSubMchId(wxPayConfig.getSubMchId());
        }

//        if(recordList.isEmpty()){
//            request.setOutOrderNo(SeqKit.genDivisionBatchId()); // 随机生成一个订单号
//        }else{
//            request.setOutOrderNo(recordList.get(0).getBatchOrderId()); //取到批次号
//        }
        request.setOutOrderNo(divisionRquest.getDivisionBatchNo()); //取到批次号
        List<DivisionReceiver> recordList = divisionRquest.getReceivers();
        List<ProfitSharingReceiver> receivers = new ArrayList<>();
        if(recordList!=null){
            for (int i = 0; i < recordList.size(); i++) {
                DivisionReceiver record = recordList.get(i);
                if(record.getDivisionAmount().compareTo(BigDecimal.ZERO)<0){
                    continue;
                }
                ProfitSharingReceiver receiver = new ProfitSharingReceiver();
                // 0-个人， 1-商户  (目前仅支持服务商appI获取个人openId, 即： PERSONAL_OPENID， 不支持 PERSONAL_SUB_OPENID )
                WxReceiverType wxReceiverType = WxReceiverType.getValid(record.getAccountType());
                receiver.setType(wxReceiverType.name());
                receiver.setAccount(record.getAccountNo());
                receiver.setAmount(Long.valueOf(Util.conversionCentAmount(record.getDivisionAmount())));
                receiver.setDescription(divisionRquest.getOrderNo() + "分账");
                receivers.add(receiver);
            }
        }
        JSONObject jsonObject = null;

        //不存在接收账号时，订单完结（解除冻结金额）。完结分账
        if(receivers.isEmpty()){
            request.setDescription("完结分账");
            jsonObject = assistService.doExecute(JsonUtil.toJsonCustom(request), WxTransactionType.DIVISION_FINISH, wxPayConfig);
        }else{
            request.setReceivers(receivers);
            jsonObject = assistService.doExecute(JsonUtil.toJsonCustom(request), WxTransactionType.DIVISION, wxPayConfig);
        }
        return processDivisionResult(jsonObject, divisionRquest.getDivisionBatchNo());
    }



    public ChannelStateRes divisionBind(DivisionReceiverBind divisionReceiverBind,PayConfig payConfig){
        WxPayConfig wxPayConfig = (WxPayConfig) payConfig;
        wxPayConfig.initLatestWxPlatCert();
        ProfitSharingReceiver profitSharingReceiver = new ProfitSharingReceiver();
        WxReceiverType valid = WxReceiverType.getValid(divisionReceiverBind.getAccountType());
        profitSharingReceiver.setType(valid.name());
        profitSharingReceiver.setAccount(divisionReceiverBind.getAccountNo());
        Certificate certificate = wxPayConfig.getWxPlatCert().getCertificate();
        profitSharingReceiver.setName(AntCertificationUtil.encryptToString(divisionReceiverBind.getAccountName(), certificate));
        profitSharingReceiver.setRelationType(divisionReceiverBind.getRelationType().name());
        profitSharingReceiver.setCustomRelation(divisionReceiverBind.getCustomRelation());
        profitSharingReceiver.setAppid(wxPayConfig.getAppId());
        // 特约商户
        if(wxPayConfig.isPartner()){
            profitSharingReceiver.setSubMchId(wxPayConfig.getSubMchId());
        }
        JSONObject jsonObject = assistService.doExecute(JsonUtil.toJsonCustom(profitSharingReceiver), WxTransactionType.DIVISION_BIND, wxPayConfig);
        ProfitSharingReceiver receiver = JSON.toJavaObject(jsonObject, ProfitSharingReceiver.class);
        if(receiver.getAccount()!=null){
            // 明确成功
            return ChannelStateRes.confirmSuccess(null);
        }
        return ChannelStateRes.fail(null);

    }

    @Override
    public BillResponse downloadBill(BillRequest downloadBillRequest, PayConfig payConfig) {
        WxPayConfig wxPayConfig = (WxPayConfig) payConfig;
        BillType billType = downloadBillRequest.getBillType();
        Date billDate = downloadBillRequest.getBillDate();
        //获取公共参数
        Map<String, Object> parameters = new HashMap<>(5);

        //目前只支持日账单
        parameters.put(WxConst.BILL_DATE, DateUtils.formatDate(billDate, DateUtils.YYYY_MM_DD));
        String fileType = billType.getFileType();
        OrderParaStructure.loadParameters(parameters, "tar_type", fileType);
        if (billType instanceof WxAccountType) {
            OrderParaStructure.loadParameters(parameters, "account_type", billType.getType());
        }
        else {
            initSubMchId(parameters,wxPayConfig).put("bill_type", billType.getType());
        }
        String body = UriVariables.getMapToParameters(parameters);
        JSONObject result = assistService.doExecute(body, WxTransactionType.valueOf(billType.getCustom()),wxPayConfig);
        String downloadUrl = result.getString("download_url");
        MethodType methodType = MethodType.GET;
        BillResponse billResponse = new BillResponse();
        billResponse.setDownloadUrl(downloadUrl);

        if(BillDataType.FILE_STEAM == downloadBillRequest.getBillDataType()){
            HttpEntity entity = assistService.buildHttpEntity(downloadUrl, "", methodType.name(),wxPayConfig);
            ResponseEntity<InputStream> responseEntity = requestTemplate.doExecuteEntity(downloadUrl, entity, InputStream.class, methodType);
            InputStream inputStream = responseEntity.getBody();
            int statusCode = responseEntity.getStatusCode();
            if (statusCode >= 400) {
                try {
                    String errorText = IOUtils.toString(inputStream);
                    JSONObject json = JSON.parseObject(errorText);
                    throw new PayErrorException(new WxPayError(statusCode + "", json.getString(WxConst.MESSAGE), errorText));
                }
                catch (IOException e) {
                    throw new PayErrorException(new WxPayError(statusCode + "", ""));
                }
            }
            billResponse.setInputStream(inputStream);
            billResponse.setBillDataType(BillDataType.FILE_STEAM);
        }

        return billResponse;
    }

    @Override
    public FundUserOpenRes fundUserOpen(FundUserOpenReq fundUserOpenReq, PayConfig payConfig) {
        return null;
    }


    private DivisionResponse processDivisionResult(JSONObject jsonObject,String DivisionBatchNo ) {
        ProfitSharingResult profitSharingResult = JsonUtil.toObject(jsonObject.toJSONString(), ProfitSharingResult.class);
        DivisionResponse response = new DivisionResponse();
        ChannelState state= ChannelState.WAITING;
        if("FINISHED".equals(profitSharingResult.getState())){
            state= ChannelState.CONFIRM_SUCCESS;
            ArrayList< DivisionResponse.Receiver> objects = new ArrayList<>();
            List<ProfitSharingResult.Receiver> receiversRes = profitSharingResult.getReceivers();
            DivisionResponse.Receiver receiverEve = new DivisionResponse.Receiver();
            if(!receiversRes.isEmpty()){
                receiversRes.forEach(item->{
                    receiverEve.setFinishTime(item.getFinishTime());
                    receiverEve.setDivisionDetailNo(item.getDetailId());
                    receiverEve.setAccount(item.getAccount());
                    receiverEve.setAmount(BigDecimal.valueOf(MoneyUtil.Fen2Yuan(item.getAmount().intValue())));
                    ChannelState channelState = null;
                    if("PENDING".equals(item.getResult())){
                        channelState = ChannelState.WAITING;
                    }else if("SUCCESS".equals(item.getResult())){
                        channelState = ChannelState.CONFIRM_SUCCESS;
                    }else{
                        channelState = ChannelState.CONFIRM_FAIL;
                    }
                    receiverEve.setChannelStateRes(ChannelStateRes.builder().msg(item.getFailReason()).channelState(channelState).build());
                    objects.add(receiverEve);
                });
                response.setReceivers(objects);

            }
        }
        response.setDivisionBatchNo(DivisionBatchNo);
        response.setOutDivisionBatchNo(profitSharingResult.getOrderId());
        response.setChannelStateRes(ChannelStateRes.builder().channelState(state).data(jsonObject).build());
        return response;
    }

    public String  getPlatCertificate(WxPayConfig payConfig){
        JSONObject jsonObject = assistService.doExecute(null, WxTransactionType.Certificate, payConfig);
        JSONArray dataArray = jsonObject.getJSONArray("data");
        // 默认认为只有一个平台证书
        JSONObject encryptObject = dataArray.getJSONObject(0);
        JSONObject encryptCertificate = encryptObject.getJSONObject("encrypt_certificate");
        String associatedData = encryptCertificate.getString("associated_data");
        String cipherText = encryptCertificate.getString("ciphertext");
        String nonce = encryptCertificate.getString("nonce");
        String serialNo = encryptObject.getString("serial_no");


        AesUtils aesUtil = new AesUtils(payConfig.getPrivateKey().toString().getBytes());


        // 平台证书密文解密
        // encrypt_certificate 中的  associated_data nonce  ciphertext
        String publicKey = null;
        try {
            publicKey = aesUtil.decryptToString(
                    associatedData.getBytes(StandardCharsets.UTF_8),
                    nonce.getBytes(StandardCharsets.UTF_8),
                    cipherText
            );
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // 获取平台证书序列号
        X509Certificate certificate = AntCertificationUtil.getCertificate(new ByteArrayInputStream(publicKey.getBytes()));

        return certificate.getSerialNumber().toString(16).toUpperCase();
    }


    private List<WxTransferDetail> initTransferDetailListAttr(Object transferDetailListAttr, WxPayConfig payConfig) {
        List<WxTransferDetail> transferDetails = null;
        if (transferDetailListAttr instanceof String) {
            transferDetails = JSON.parseArray((String) transferDetailListAttr, WxTransferDetail.class);
        }
        else if (null != transferDetailListAttr) {
            //偷懒的做法
            transferDetails = JSON.parseArray(JSON.toJSONString(transferDetailListAttr), WxTransferDetail.class);
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

    public static void main(String[] args){
        ProfitSharingReceiver receiver = new ProfitSharingReceiver();
        receiver.setSubMchId("sub_mch_12345");
        receiver.setAppid("wx1234567890");


        Gson gson = new GsonBuilder().create();
        String jsonString = gson.toJson(receiver);

        System.out.println(JsonUtil.toJsonCustom(receiver));

        System.out.println(jsonString);
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
        String transactionId = request.getOutOrderNo();
        String outTradeNo = request.getOrderNo();
        Map<String, Object> attr = new HashMap<>();
        OrderParaStructure.loadParameters(attr, wxPayConfig.isPartner() ? WxConst.SP_MCH_ID : WxConst.MCH_ID, wxPayConfig.getMchId());
        String parameters = UriVariables.getMapToParameters(attr);
        WxTransactionType transactionType = WxTransactionType.QUERY_TRANSACTION_ID;
        String uriVariable = transactionId;
        if (StringUtils.isNotBlank(outTradeNo)) {
            transactionType = WxTransactionType.QUERY_OUT_TRADE_NO;
            uriVariable = outTradeNo;
        }
        PayResponse build = new WxPayResponse();
        JSONObject resultJSON = assistService.doExecute(parameters, transactionType, wxPayConfig, uriVariable);
        String channelState = resultJSON.getString("trade_state");
        ChannelState channelRes = ChannelState.WAITING;
        channelRes = WxTradeStatusEnum.getChannelState(channelState);
        if(ChannelState.CONFIRM_SUCCESS == channelRes){
            build.setOutTradeNo(resultJSON.getString("transaction_id"));
        }

//        if ("SUCCESS".equals(channelState)) {
//            channelRes =ChannelState.CONFIRM_SUCCESS;
//            String transaction_id = resultJSON.getString("transaction_id");
//            build.setOutTradeNo(transaction_id);
//
//        }else if("USERPAYING".equals(channelState) || "NOTPAY".equals(channelState)){ //支付中，等待用户输入密码
//            //支付中
//        }else if("CLOSED".equals(channelState)
//                || "REVOKED".equals(channelState)
//                || "PAYERROR".equals(channelState)){  //CLOSED—已关闭， REVOKED—已撤销(刷卡支付), PAYERROR--支付失败(其他原因，如银行返回失败)
//            channelRes = ChannelState.CONFIRM_FAIL; //支付失败
//        }else{
//            channelRes = ChannelState.UNKNOWN;
//        }
        build.setOrderNo(resultJSON.getString("out_trade_no"));
        build.setMsg(resultJSON.getString("trade_state_desc"));
        build.setFinishTime(resultJSON.getString("success_time"));
        JSONObject payer = resultJSON.getJSONObject("payer");
        if(payer!=null){
            build.setPayerAccount(payer.getString("openid"));
            build.setPayerAccountType("openid");
        }
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
        String parameters = UriVariables.getMapToParameters(initSubMchId(null,wxPayConfig));
        resultJSON =  assistService.doExecute(parameters, WxTransactionType.REFUND_QUERY,wxPayConfig, refundNo);
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

    /**
     * 转账
     * @param order
     * @param payConfig
     */
    @Override
    public TransferResponse transfer(TransferOrder order, PayConfig payConfig) {
        TransferOrderBatch batch = new TransferOrderBatch();
        batch.setTransferDetails(Arrays.asList(order));
        batch.setBatchName(order.getAccountName());
        batch.setRemark(order.getRemark());
        batch.setTotalAmount(order.getAmount());
        batch.setTotalNum(1);
        batch.setTransferBatchNo(order.getTransferNo());
        return this.transferBatch(batch, payConfig);
    }

    public  Map<String, Object>  transferBatchQuery(WxTransferQueryOrder transferQueryOrder,WxPayConfig wxPayConfig){
        Map<String, Object> parameters = new HashMap<>(6);
        TransactionType transactionType = transferQueryOrder.getTransactionType();
        List<Object> uriVariables = new ArrayList<>(3);

        if (StringUtils.isNotEmpty(transferQueryOrder.getDetailId())) {
             uriVariables.add(transferQueryOrder.getDetailId());
        }
        else if (StringUtils.isNotEmpty(transferQueryOrder.getOutDetailNo())) {
             uriVariables.add(transferQueryOrder.getOutDetailNo());
        }

        if (transactionType == WxTransferType.QUERY_BATCH_BY_BATCH_ID || transactionType == WxTransferType.QUERY_BATCH_BY_OUT_BATCH_NO) {

            if(transferQueryOrder.getNeedQueryDetail()!=null){
                parameters.put( WxConst.NEED_QUERY_DETAIL, transferQueryOrder.getNeedQueryDetail()) ;
            }
            if(transferQueryOrder.getOffset()!=null){
                parameters.put( WxConst.OFFSET, transferQueryOrder.getOffset()) ;
            }
            if(transferQueryOrder.getLimit()!=null){
                parameters.put( WxConst.LIMIT, transferQueryOrder.getLimit()) ;
            }
            if(transferQueryOrder.getLimit()!=null){
                parameters.put( WxConst.DETAIL_STATUS, transferQueryOrder.getDetailStatus()) ;
            }
        }
        String requestBody = JSON.toJSONString(parameters);
        return assistService.doExecute(requestBody, transferQueryOrder.getTransactionType(),wxPayConfig, uriVariables.toArray());

    }


    @Override
    public TransferResponse transferQuery(TransferQueryRequest request, PayConfig payConfig) {
        WxTransferQueryOrder queryOrder = new WxTransferQueryOrder();
        queryOrder.setOutDetailNo(request.getTransferNo());
        queryOrder.setOutBatchNo(request.getTransferNo());
        queryOrder.setTransactionType(WxTransferType.QUERY_BATCH_DETAIL_BY_OUT_BATCH_NO);
        Map<String, Object> map = this.transferBatchQuery(queryOrder, (WxPayConfig) payConfig);
        Object batch_id = map.get("batch_id");
        ChannelState channelState = null;
        if(batch_id!=null){
            channelState = ChannelState.CONFIRM_SUCCESS;
        }else{
            channelState = ChannelState.CONFIRM_FAIL;
        }
        ChannelStateRes build = ChannelStateRes.builder().channelState(channelState).build();
        return TransferResponse.builder().channelStateRes(build).build();
    }

    @Override
    public SettleResponse settle(SettleRequest settleRequest, PayConfig payConfig) {
        return null;
    }

    /**
     * 解冻 ; 调用分账接口后，需要解冻剩余资金时，调用本接口将剩余的分账金额全部解冻给本商户
     *
     * @param unfreezeRequest
     * @param payConfig
     */
    @Override
    public ChannelStateRes unfreeze(UnfreezeRequest unfreezeRequest, PayConfig payConfig) {
        ProfitSharingRequest request = new ProfitSharingRequest();
        request.setTransactionId(unfreezeRequest.getOutTradeNo());
        WxPayConfig wxPayConfig = (WxPayConfig) payConfig;
        request.setAppid(wxPayConfig.getAppId());
        // 特约商户
        if(wxPayConfig.isPartner()){
            request.setSubMchId(wxPayConfig.getSubMchId());
        }
        request.setOutOrderNo(unfreezeRequest.getDivisionBatchNo()); //取到批次号

        JSONObject jsonObject = null;
        request.setDescription("解冻全部剩余资金");
        jsonObject = assistService.doExecute(JsonUtil.toJsonCustom(request), WxTransactionType.DIVISION_FINISH, wxPayConfig);


        ChannelState state= ChannelState.WAITING;
        if("FINISHED".equals(jsonObject.get("state"))){
            state= ChannelState.CONFIRM_SUCCESS;
        }
        return ChannelStateRes.builder().channelState(state).data(jsonObject).build();
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
    public void loadSettleInfo(Map<String, Object> parameters, WxPayRequest order) {
        Object profitSharing = order.getAttr("profit_sharing");
        if(profitSharing == null){
            profitSharing = order.isDivision();
        }
        if (null != profitSharing) {
            Map<String, Object> settleInfo = new MapGen<>("profit_sharing", profitSharing).getAttr();
            parameters.put("settle_info", settleInfo);
            return;
        }
        //结算信息
        OrderParaStructure.loadParameters(parameters, "settle_info", order);
    }



}


