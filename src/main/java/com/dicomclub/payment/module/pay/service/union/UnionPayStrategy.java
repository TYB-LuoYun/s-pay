package com.dicomclub.payment.module.pay.service.union;

import com.alibaba.fastjson.JSONObject;
import com.dicomclub.payment.exception.PayError;
import com.dicomclub.payment.exception.PayErrorException;
import com.dicomclub.payment.exception.PayException;
import com.dicomclub.payment.module.pay.common.ChannelStateRes;
import com.dicomclub.payment.module.pay.config.PayConfig;
import com.dicomclub.payment.module.pay.config.UnionPayConfig;
import com.dicomclub.payment.module.pay.constants.UnionPayConstants;
import com.dicomclub.payment.module.pay.enums.ChannelState;
import com.dicomclub.payment.module.pay.enums.PayChannel;
import com.dicomclub.payment.module.pay.enums.PayDataType;
import com.dicomclub.payment.module.pay.model.*;
import com.dicomclub.payment.module.pay.model.union.UnionPayRequest;
import com.dicomclub.payment.module.pay.model.union.UnionPayResponse;
import com.dicomclub.payment.module.pay.model.wxpay.BillResponse;
import com.dicomclub.payment.module.pay.model.wxpay.DivisionReceiverBind;
import com.dicomclub.payment.module.pay.model.wxpay.BillRequest;
import com.dicomclub.payment.module.pay.service.PayStrategy;
import com.dicomclub.payment.module.pay.service.huifu.model.FundUserOpenReq;
import com.dicomclub.payment.module.pay.service.huifu.model.FundUserOpenRes;
import com.dicomclub.payment.module.pay.service.union.common.OrderParaStructure;
import com.dicomclub.payment.module.pay.service.union.common.UnionTransactionType;
import com.dicomclub.payment.module.pay.service.union.common.sign.CertDescriptor;
import com.dicomclub.payment.module.pay.service.union.common.sign.SignTextUtils;
import com.dicomclub.payment.module.pay.service.union.common.sign.SignUtils;
import com.dicomclub.payment.module.pay.service.union.common.sign.encrypt.RSA;
import com.dicomclub.payment.module.pay.service.union.common.sign.encrypt.RSA2;
import com.dicomclub.payment.module.pay.service.union.common.Util;
import com.dicomclub.payment.util.DateUtils;
import com.dicomclub.payment.util.MoneyUtil;
import com.dicomclub.payment.util.WebUtil;
import com.dicomclub.payment.util.httpRequest.HttpConfigStorage;
import com.dicomclub.payment.util.httpRequest.HttpRequestTemplate;
import com.dicomclub.payment.util.httpRequest.UriVariables;
import lombok.extern.slf4j.Slf4j;
import net.glxn.qrgen.core.image.ImageType;
import net.glxn.qrgen.javase.QRCode;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.cert.*;
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author ftm
 * @date 2023/2/17 0017 17:48
 */
@Slf4j
@Component
@Primary
public class UnionPayStrategy extends PayStrategy {


  @Autowired
  private HttpRequestTemplate requestTemplate  ;

  /**
   * 测试域名
   */
  private static final String TEST_BASE_DOMAIN = "test.95516.com";
  /**
   * 正式域名
   */
  private static final String RELEASE_BASE_DOMAIN = "95516.com";
  /**
   * 交易请求地址
   */
  private static final String FRONT_TRANS_URL = "https://gateway.%s/gateway/api/frontTransReq.do";
  private static final String BACK_TRANS_URL = "https://gateway.%s/gateway/api/backTransReq.do";
  private static final String SINGLE_QUERY_URL = "https://gateway.%s/gateway/api/queryTrans.do";
  private static final String BATCH_TRANS_URL = "https://gateway.%s/gateway/api/batchTrans.do";
  private static final String FILE_TRANS_URL = "https://filedownload.%s/";
  private static final String APP_TRANS_URL = "https://gateway.%s/gateway/api/appTransReq.do";
  private static final String CARD_TRANS_URL = "https://gateway.%s/gateway/api/cardTransReq.do";


  public UnionPayStrategy(){
      //请求连接池配置
      HttpConfigStorage httpConfigStorage = new HttpConfigStorage();
      //最大连接数
      httpConfigStorage.setMaxTotal(20);
      //默认的每个路由的最大连接数
      httpConfigStorage.setDefaultMaxPerRoute(10);
      requestTemplate = new HttpRequestTemplate(httpConfigStorage);
  }





  /**
   * 订单预支付
   *
   * @param payRequest
   * @return
   */
  @Override
  public PayResponse pay(PayRequest payRequest, PayConfig payConfig) {
    UnionPayConfig unionPayConfig = (UnionPayConfig) payConfig;
//      证书,这个可以缓存起来
    CertDescriptor certDescriptor = initCertDescriptor(unionPayConfig);
    Map<String, Object> params = orderInfo((UnionPayRequest) payRequest, unionPayConfig);
//        params = preOrderHandler(params, order);
    Map<String, Object> map = setSign(params ,unionPayConfig,certDescriptor);
    PayChannel type = payRequest.getPayChannel();
    UnionPayResponse response = new UnionPayResponse();
    switch (type) {
      case UNION_WEB_GATEWAY:
        String directHtml = buildRequest(map,unionPayConfig);
        if(payRequest.getPayDataType()!=null&&payRequest.getPayDataType() == PayDataType.PAY_URL){
          response.setPayUrl(getUnionRequestUrl(params, unionPayConfig));
        }else if(payRequest.getPayDataType() == PayDataType.FORM){
          response.setFormContent(buildForm(map,unionPayConfig));
        }else{
          response.setBody(directHtml);
        }
        break;
      case UNION_QRCODE:
        String responseStr = requestTemplate.postForObject(String.format(BACK_TRANS_URL, getReqUrl(unionPayConfig.isSandbox())), params, String.class);
        JSONObject res  = UriVariables.getParametersToMap(responseStr);
        if (res.isEmpty()) {
          throw new PayException( "failure 响应内容有误!" + responseStr);
        }
        if (this.verify(res,unionPayConfig)) {
          if (UnionPayConstants.OK_RESP_CODE.equals(res.get(UnionPayConstants.param_respCode))) {
            //成功
            String qrCode =  (String)res.get(UnionPayConstants.param_qrCode);
            this.handleQrcodeResult(qrCode,payRequest.getPayDataType(),response);
            response.setCodeUrl(qrCode);
          }else{
            throw new PayException((String) res.get(UnionPayConstants.param_respCode), res.get(UnionPayConstants.param_respMsg)+res.toJSONString());
          }
         }
        break;
      default:
        throw new PayException("暂未实现");
    }



    return response;
  }

  private boolean verify(Map res,UnionPayConfig unionPayConfig) {
    return this.signVerify(res, (String) res.get(UnionPayConstants.param_signature),unionPayConfig);
  }


  public Map<String, Object> orderInfo(UnionPayRequest order ,UnionPayConfig unionPayConfig){
    Map<String, Object> params = this.getCommonParam(unionPayConfig);
    PayChannel type = order.getPayChannel();
//      后台通知地址
    OrderParaStructure.loadParameters(params, UnionPayConstants.param_backUrl, unionPayConfig.getNotifyUrl());
    OrderParaStructure.loadParameters(params, UnionPayConstants.param_backUrl,order.getNotifyUrl());





    params.put(UnionPayConstants.param_orderId, order.getOrderNo());

    if (StringUtils.isNotEmpty(order.getAttach())) {
      params.put(UnionPayConstants.param_reqReserved, order.getAttach());
    }
    switch (type) {
      case UNION_MOBILE_WAP:
        UnionTransactionType.WAP.convertMap(params);
      case UNION_WEB_GATEWAY:
        UnionTransactionType.WEB.convertMap(params);
        //todo PCwap网关跳转支付特殊用法.txt
      case UNION_B2B:
        // 总金额(单位是分)
        params.put(UnionPayConstants.param_txnAmt,  Util.conversionCentAmount(order.getOrderAmount()));
        params.put("orderDesc", order.getOrderName());
        params.put(UnionPayConstants.param_payTimeout, getPayTimeout(order.getExpirationTime()));
        if(unionPayConfig.getReturnUrl() != null){
          params.put(UnionPayConstants.param_frontUrl, unionPayConfig.getReturnUrl());
        }else if(params.get(UnionPayConstants.param_backUrl)!=null ){
          params.put(UnionPayConstants.param_frontUrl, params.get(UnionPayConstants.param_backUrl));
        }

        break;
      case UNION_CONSUME_BARCODE:
        params.put(UnionPayConstants.param_txnAmt, Util.conversionCentAmount(order.getOrderAmount()));
        params.put(UnionPayConstants.param_qrNo, order.getAuthCode());
        break;
      case UNION_QRCODE:
        UnionTransactionType.APPLY_QR_CODE.convertMap(params);
        if (null != order.getOrderAmount()) {
          params.put(UnionPayConstants.param_txnAmt, Util.conversionCentAmount(order.getOrderAmount()));
        }
        params.put(UnionPayConstants.param_payTimeout, getPayTimeout(order.getExpirationTime()));
        break;
      default:
        params.put(UnionPayConstants.param_txnAmt, Util.conversionCentAmount(order.getOrderAmount()));
        params.put(UnionPayConstants.param_payTimeout, getPayTimeout(order.getExpirationTime()));
        params.put("orderDesc", order.getOrderName());
    }
    if(order.getAttrs() != null){
      params.putAll(order.getAttrs());
    }
    return params;
  }

  private CertDescriptor initCertDescriptor(UnionPayConfig unionPayConfig) {
    try {
      CertDescriptor certDescriptor = new CertDescriptor();
      certDescriptor.initPrivateSignCert(unionPayConfig.getKeyPrivateCertInputStream(), unionPayConfig.getKeyPrivateCertPwd(), "PKCS12");
      certDescriptor.initPublicCert(unionPayConfig.getAcpMiddleCertInputStream());
      certDescriptor.initRootCert(unionPayConfig.getAcpRootCertInputStream());
      return certDescriptor;
    } catch (Exception e) {
      log.error("", e);
      throw new PayException("银联证书初始化错误");
    }
  }


  private String getUnionRequestUrl(Map<String, Object> params, UnionPayConfig unionPayConfig) {
    StringBuffer urlSb = new StringBuffer(String.format(FRONT_TRANS_URL, getReqUrl( unionPayConfig.getSandbox())) );
    try {
      String sysMustQuery = WebUtil.buildQueryParams(params, unionPayConfig.getInputCharset());
      urlSb.append("?");
      urlSb.append(sysMustQuery);
    } catch (IOException e) {
      e.printStackTrace();
    }
    return urlSb.toString();
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
//  处理字符串参数
    Map<String, Object> dataMap = JSONObject.parseObject(notifyData);
//  验签
    if (null == dataMap || dataMap.get(UnionPayConstants.param_signature) == null) {
      log.debug("银联支付验签异常：params：" + dataMap);
      throw new PayException("银联验签异常!");
    }
    boolean sign = this.signVerify(dataMap, (String) dataMap.get(UnionPayConstants.param_signature), (UnionPayConfig) payConfig);
    if(!sign){
      throw new PayException("银联签名校验失败!");
    }

    UnionPayResponse payResponse = new UnionPayResponse();
    payResponse.setChannelState(ChannelState.WAITING);
    //交易状态
    if (UnionPayConstants.OK_RESP_CODE.equals(dataMap.get(UnionPayConstants.param_respCode))) {
      //这里进行成功的处理
      payResponse.setChannelState(ChannelState.CONFIRM_SUCCESS);
      payResponse.setOutTradeNo((String)dataMap.get(UnionPayConstants.param_queryId));
      payResponse.setOrderNo((String)dataMap.get(UnionPayConstants.param_orderId));
      payResponse.setFinishTime((String)dataMap.get(UnionPayConstants.param_txnTime));
    }
    else{
//      payResponse.setChannelState(ChannelState.CONFIRM_FAIL);//可能是支付失败，用户可能重新支付，所以这里不能确认
        payResponse.setCode((String)dataMap.get(UnionPayConstants.param_respCode));
        payResponse.setMsg((String)dataMap.get(UnionPayConstants.param_respMsg));
    }

    if(payResponse.getChannelState() == ChannelState.CONFIRM_SUCCESS){
      return buildPayResponse(dataMap ,payResponse);
    }
//      其他情况非终态
    return payResponse;

  }

  /**
   * 异步通知-退款
   * 与支付结果不同的是，退款结果可能会受到更多的影响因素，比如退款金额、退款原因、退款渠道等，因此退款处理的时间可能会比支付处理更长。通过异步回调接口，商户可以及时获取退款结果，以便及时处理相关业务。
   * @param request
   * @param payConfig
   * @return
   */
  @Override
  public RefundResponse asyncNotifyRefund(HttpServletRequest request, PayConfig payConfig) {
    return null;
  }


  /**
   * 订单结果查询
   *
   * @param request
   * @param payConfig
   */
  @Override
  public PayResponse query(OrderQueryRequest request, PayConfig payConfig) {
      UnionPayConfig unionPayConfig = (UnionPayConfig)payConfig;
      Map<String, Object> params = this.getCommonParam(unionPayConfig);
      UnionTransactionType.QUERY.convertMap(params);
      params.put(UnionPayConstants.param_orderId, request.getOrderNo());
      this.setSign(params,unionPayConfig,initCertDescriptor(unionPayConfig));
      String responseStr = requestTemplate.postForObject(String.format(SINGLE_QUERY_URL, getReqUrl(unionPayConfig.isSandbox())), params, String.class);
      JSONObject response = UriVariables.getParametersToMap(responseStr);
      boolean sign = this.signVerify(response, (String) response.get(UnionPayConstants.param_signature), unionPayConfig);
      if(!sign){
          throw new PayException("failure 银联验证签名失败");
      }
    UnionPayResponse unionPayResponse = new UnionPayResponse();
      ChannelState channelState = ChannelState.WAITING;
      if (UnionPayConstants.OK_RESP_CODE.equals(response.getString(UnionPayConstants.param_respCode))) {
          String origRespCode = response.getString(UnionPayConstants.param_origRespCode);
          if ((UnionPayConstants.OK_RESP_CODE).equals(origRespCode)) {
              //交易成功，更新商户订单状态
              channelState =ChannelState.CONFIRM_SUCCESS;
          }
      }
    unionPayResponse.setChannelState(channelState);
    unionPayResponse.setOutTradeNo(response.getString(UnionPayConstants.param_queryId));
    unionPayResponse.setOrderNo(response.getString(UnionPayConstants.param_orderId));
    unionPayResponse.setMsg(response.getString(UnionPayConstants.param_origRespMsg));
    unionPayResponse.setFinishTime(response.getString(UnionPayConstants.param_txnTime));
    return unionPayResponse;
  }

  /**
   * 退款
   *
   * @param payConfig
   */
  @Override
  public RefundResponse refund(RefundRequest refundOrder, PayConfig payConfig) {
      UnionTransactionType type = UnionTransactionType.REFUND;
      UnionPayConfig unionPayConfig = (UnionPayConfig) payConfig;
//      证书,这个可以缓存起来
      CertDescriptor certDescriptor = initCertDescriptor(unionPayConfig);
      Map<String, Object> params = this.getCommonParam(unionPayConfig);
      type.convertMap(params);
      params.put(UnionPayConstants.param_orderId, refundOrder.getRefundNo());
      params.put(UnionPayConstants.param_txnAmt, Util.conversionCentAmount(refundOrder.getRefundAmount()));
      params.put(UnionPayConstants.param_origQryId, refundOrder.getOrderNo());
      params.putAll(refundOrder.getAttrs());
      this.setSign(params,unionPayConfig,certDescriptor);
      String responseStr = requestTemplate.postForObject(String.format(BACK_TRANS_URL, getReqUrl(unionPayConfig.isSandbox())), params, String.class);
      JSONObject response = UriVariables.getParametersToMap(responseStr);
      if (null == response || response.get(UnionPayConstants.param_signature) == null) {
          log.debug("银联支付验签异常：params：" + response);
         throw new PayException("银联支付验签异常：params：" + response);
      }
      boolean b = this.signVerify(response, (String) response.get(UnionPayConstants.param_signature), unionPayConfig);
      if(b == false){
          throw new PayException("银联支付验签失败");
      }

      ChannelStateRes channelStateRes = new ChannelStateRes();
      RefundResponse refundResponse = new RefundResponse();
      channelStateRes.setChannelState(ChannelState.WAITING);
      String status = (String) response.get(UnionPayConstants.param_respCode);
      if(UnionPayConstants.OK_RESP_CODE.equals(status)){
//          String refundId = resultJSON.getString("refund_id");
//          channelStateRes.setChannelState(ChannelState.CONFIRM_SUCCESS);
//          refundResponse.setRefundId(refundId);
          //交易已受理(不代表交易已成功），等待接收后台通知更新订单状态,也可以主动发起 查询交易确定交易状态。
      }else if (("03").equals(status)|| ("04").equals(status)|| ("05").equals(status)){ // 退款处理中
          //后续需发起交易状态查询交易确定交易状态
      }else{
          //其他应答码为失败请排查原因
          channelStateRes.setChannelState(ChannelState.CONFIRM_FAIL);
          channelStateRes.setMsg(status);
          throw new PayException(response.getString(UnionPayConstants.param_respCode), response.getString(UnionPayConstants.param_respMsg) );
      }
      refundResponse.setChannelStateRes(channelStateRes);
      return refundResponse;



  }

  /**
   * 退款查询
   *
   * @param payConfig
   */
  public RefundResponse refundQuery(RefundQueryRequest refundQueryRequest, PayConfig payConfig) {
    String refundNo = refundQueryRequest.getRefundNo();
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

  @Override
  public FundUserOpenRes fundUserOpen(FundUserOpenReq fundUserOpenReq, PayConfig payConfig) {
    return null;
  }




  private PayResponse buildPayResponse(Map<String, Object> dataMap, UnionPayResponse payResponse) {
        payResponse.setOrderAmount(MoneyUtil.Fen2Yuan(Integer.valueOf((String)dataMap.get(UnionPayConstants.param_settleAmt))));
        payResponse.setOrderNo((String)dataMap.get(UnionPayConstants.param_orderId));
//    payResponse.setOutTradeNo(response.getTradeNo());
        payResponse.setAttach((String)dataMap.get(UnionPayConstants.param_reqReserved));
        return payResponse;
    }


    /**
   * 银联全渠道系统，产品参数，除了encoding自行选择外其他不需修改
   *
   * @return 返回参数集合
   */
  private Map<String, Object> getCommonParam(UnionPayConfig unionPayConfig) {
    Map<String, Object> params = new TreeMap<>();
    UnionPayConfig  configStorage = unionPayConfig;
    //银联接口版本
    params.put(UnionPayConstants.param_version, configStorage.getVersion());
    //编码方式
    params.put(UnionPayConstants.param_encoding, unionPayConfig.getInputCharset().toUpperCase());
    //商户代码
    params.put(UnionPayConstants.param_merId, unionPayConfig.getPid());

    //订单发送时间
    params.put(UnionPayConstants.param_txnTime, DateUtils.formatDate(new Date(), DateUtils.YYYYMMDDHHMMSS));
    //后台通知地址
    params.put(UnionPayConstants.param_backUrl, unionPayConfig.getNotifyUrl());

    //交易币种
    params.put(UnionPayConstants.param_currencyCode, "156");
    //接入类型，商户接入填0 ，不需修改（0：直连商户， 1： 收单机构 2：平台商户）
    params.put(UnionPayConstants.param_accessType, configStorage.getAccessType());
    return params;
  }



  /**
   * 订单超时时间。
   * 超过此时间后，除网银交易外，其他交易银联系统会拒绝受理，提示超时。 跳转银行网银交易如果超时后交易成功，会自动退款，大约5个工作日金额返还到持卡人账户。
   * 此时间建议取支付时的北京时间加15分钟。
   * 超过超时时间调查询接口应答origRespCode不是A6或者00的就可以判断为失败。
   *
   * @param expirationTime 超时时间
   * @return 具体的时间字符串
   */
  private String getPayTimeout(Date expirationTime) {
    //
    if (null != expirationTime) {
      return DateUtils.formatDate(expirationTime, DateUtils.YYYYMMDDHHMMSS);
    }
    return DateUtils.formatDate(new Timestamp(System.currentTimeMillis() + 30 * 60 * 1000), DateUtils.YYYYMMDDHHMMSS);
  }



  /**
   * 生成并设置签名
   *
   * @param parameters 请求参数
   * @return 请求参数
   */
  private Map<String, Object> setSign(Map<String, Object> parameters,UnionPayConfig unionPayConfig,CertDescriptor certDescriptor) {

    SignUtils signUtils = SignUtils.valueOf(unionPayConfig.getSignType());

    String signStr;
    switch (signUtils) {
      case RSA:
        parameters.put(UnionPayConstants.param_signMethod, UnionPayConstants.SIGNMETHOD_RSA);
        parameters.put(UnionPayConstants.param_certId, certDescriptor.getSignCertId());
        signStr = SignUtils.SHA1.createSign(SignTextUtils.parameterText(parameters, "&", "signature"), "", unionPayConfig.getInputCharset());
        parameters.put(UnionPayConstants.param_signature, RSA.sign(signStr, certDescriptor.getSignCertPrivateKey(unionPayConfig.getKeyPrivateCertPwd()), unionPayConfig.getInputCharset()));
        break;
      case RSA2:
        parameters.put(UnionPayConstants.param_signMethod, UnionPayConstants.SIGNMETHOD_RSA);
        parameters.put(UnionPayConstants.param_certId, certDescriptor.getSignCertId());
        signStr = SignUtils.SHA256.createSign(SignTextUtils.parameterText(parameters, "&", "signature"), "", unionPayConfig.getInputCharset());
        parameters.put(UnionPayConstants.param_signature, RSA2.sign(signStr, certDescriptor.getSignCertPrivateKey(unionPayConfig.getKeyPrivateCertPwd()), unionPayConfig.getInputCharset()));
        break;
      case SHA1:
      case SHA256:
      case SM3:
        String key = unionPayConfig.getKeyPrivate();
        signStr = SignTextUtils.parameterText(parameters, "&", "signature");
        key = signUtils.createSign(key, "", unionPayConfig.getInputCharset()) + "&";
        parameters.put(UnionPayConstants.param_signature, signUtils.createSign(signStr, key, unionPayConfig.getInputCharset()));
        break;
      default:
        throw new PayException("sign fail 未找到的签名类型");
    }


    return parameters;
  }

  /**
   * 功能：生成自动跳转的Html表单
   *
   * @param orderInfo 发起支付的订单信息
   * @return 生成自动跳转的Html表单返回给支付端, 针对于PC端
   */
  public String buildRequest(Map<String, Object> orderInfo,UnionPayConfig unionPayConfig) {
    StringBuffer sf = new StringBuffer();
    sf.append("<html><head><meta http-equiv='Content-Type' content='text/html; charset=" + unionPayConfig.getInputCharset() + "'/></head><body>");
    sf.append("<form id = 'pay_form' action='" + String.format(FRONT_TRANS_URL, getReqUrl( unionPayConfig.getSandbox())) + "' method='post'>");
    if (null != orderInfo && 0 != orderInfo.size()) {
      for (Map.Entry<String, Object> entry : orderInfo.entrySet()) {
        String key = entry.getKey();
        Object value = entry.getValue();
        sf.append("<input type='hidden' name='" + key + "' id='" + key + "' value='" + value + "'/>");
      }
    }
    sf.append("</form>");
    sf.append("</body>");
    sf.append("<script type='text/javascript'>");
    sf.append("document.all.pay_form.submit();");
    sf.append("</script>");
    sf.append("</html>");
    return sf.toString();
  }

  private String buildForm(Map<String, Object> orderInfo, UnionPayConfig unionPayConfig) {
    StringBuffer sf = new StringBuffer();
    sf.append("<form id = 'pay_form' action='" + String.format(FRONT_TRANS_URL, getReqUrl( unionPayConfig.getSandbox())) + "' method='post'>");
    if (null != orderInfo && 0 != orderInfo.size()) {
      for (Map.Entry<String, Object> entry : orderInfo.entrySet()) {
        String key = entry.getKey();
        Object value = entry.getValue();
        sf.append("<input type='hidden' name='" + key + "'id='" + key + "' value='" + value + "'/>");
      }
    }
    sf.append("</form>");
    sf.append("<script>document.getElementById('pay_form').submit();</script>");
    return sf.toString();
  }



  /**
   * 获取支付请求根地址
   *
   */
  public String getReqUrl(Boolean isSandBox) {
    return (isSandBox ? TEST_BASE_DOMAIN : RELEASE_BASE_DOMAIN);
  }


  /**
   * 签名校验
   *
   * @param params 参数集
   * @param sign   签名原文
   * @return 签名校验 true通过
   */
  public boolean signVerify(Map<String, Object> params, String sign ,UnionPayConfig payConfigStorage) {
    CertDescriptor certDescriptor = initCertDescriptor(payConfigStorage);
    SignUtils signUtils = SignUtils.valueOf(payConfigStorage.getSignType());

    String data = SignTextUtils.parameterText(params, "&", "signature");
    switch (signUtils) {
      case RSA:
        data = SignUtils.SHA1.createSign(data, "", payConfigStorage.getInputCharset());
        return RSA.verify(data, sign, verifyCertificate(certDescriptor,genCertificateByStr((String) params.get(UnionPayConstants.param_signPubKeyCert))).getPublicKey(), payConfigStorage.getInputCharset());
      case RSA2:
        data = SignUtils.SHA256.createSign(data, "", payConfigStorage.getInputCharset());
        return RSA2.verify(data, sign, verifyCertificate(certDescriptor,genCertificateByStr((String) params.get(UnionPayConstants.param_signPubKeyCert))).getPublicKey(), payConfigStorage.getInputCharset());
      case SHA1:
      case SHA256:
      case SM3:
        String before = signUtils.createSign(payConfigStorage.getKeyPublic(), "", payConfigStorage.getInputCharset());
        return signUtils.verify(data, sign, "&" + before, payConfigStorage.getInputCharset());
      default:
        return false;
    }
  }



  /**
   * 将字符串转换为X509Certificate对象.
   *
   * @param x509CertString 证书串
   * @return X509Certificate
   */
  public static X509Certificate genCertificateByStr(String x509CertString) {
    X509Certificate x509Cert = null;
    try {
      CertificateFactory cf = CertificateFactory.getInstance("X.509");
      InputStream tIn = new ByteArrayInputStream(x509CertString.getBytes("ISO-8859-1"));
      x509Cert = (X509Certificate) cf.generateCertificate(tIn);
    }
    catch (Exception e) {
      e.printStackTrace();
      throw new PayException("证书加载失败"+ "gen certificate error:" + e.getLocalizedMessage());
    }
    return x509Cert;
  }


  /**
   * 验证证书链
   *
   * @param cert 需要验证的证书
   */
  private X509Certificate verifyCertificate(CertDescriptor certDescriptor,X509Certificate cert) {
    try {
      cert.checkValidity();//验证有效期
      X509Certificate middleCert = certDescriptor.getPublicCert();
      X509Certificate rootCert = certDescriptor.getRootCert();

      X509CertSelector selector = new X509CertSelector();
      selector.setCertificate(cert);

      Set<TrustAnchor> trustAnchors = new HashSet<TrustAnchor>();
      trustAnchors.add(new TrustAnchor(rootCert, null));
      PKIXBuilderParameters pkixParams = new PKIXBuilderParameters(trustAnchors, selector);

      Set<X509Certificate> intermediateCerts = new HashSet<X509Certificate>();
      intermediateCerts.add(rootCert);
      intermediateCerts.add(middleCert);
      intermediateCerts.add(cert);

      pkixParams.setRevocationEnabled(false);

      CertStore intermediateCertStore = CertStore.getInstance("Collection", new CollectionCertStoreParameters(intermediateCerts));
      pkixParams.addCertStore(intermediateCertStore);

      CertPathBuilder builder = CertPathBuilder.getInstance("PKIX");

      /*PKIXCertPathBuilderResult result = (PKIXCertPathBuilderResult)*/
      builder.build(pkixParams);
      return cert;
    }
    catch (java.security.cert.CertPathBuilderException e) {
      log.error("verify certificate chain fail.", e);
    }
    catch (CertificateExpiredException e) {
      log.error("", e);
    }
    catch (GeneralSecurityException e) {
      log.error("", e);
    }
    return null;
  }



}
