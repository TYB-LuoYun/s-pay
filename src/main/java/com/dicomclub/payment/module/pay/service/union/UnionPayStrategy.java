package com.dicomclub.payment.module.pay.service.union;

import com.alibaba.fastjson.JSONObject;
import com.dicomclub.payment.exception.PayException;
import com.dicomclub.payment.module.pay.config.PayConfig;
import com.dicomclub.payment.module.pay.config.UnionPayConfig;
import com.dicomclub.payment.module.pay.constants.AliPayConstants;
import com.dicomclub.payment.module.pay.constants.UnionPayConstants;
import com.dicomclub.payment.module.pay.enums.ChannelState;
import com.dicomclub.payment.module.pay.enums.PayChannel;
import com.dicomclub.payment.module.pay.enums.PayDataType;
import com.dicomclub.payment.module.pay.model.OrderQueryRequest;
import com.dicomclub.payment.module.pay.model.OrderQueryResponse;
import com.dicomclub.payment.module.pay.model.PayRequest;
import com.dicomclub.payment.module.pay.model.PayResponse;
import com.dicomclub.payment.module.pay.model.union.UnionPayRequest;
import com.dicomclub.payment.module.pay.model.union.UnionPayResponse;
import com.dicomclub.payment.module.pay.service.PayStrategy;
import com.dicomclub.payment.module.pay.service.union.common.OrderParaStructure;
import com.dicomclub.payment.module.pay.service.union.common.UnionTransactionType;
import com.dicomclub.payment.module.pay.service.union.common.sign.CertDescriptor;
import com.dicomclub.payment.module.pay.service.union.common.sign.SignTextUtils;
import com.dicomclub.payment.module.pay.service.union.common.sign.SignUtils;
import com.dicomclub.payment.module.pay.service.union.common.sign.encrypt.RSA;
import com.dicomclub.payment.module.pay.service.union.common.sign.encrypt.RSA2;
import com.dicomclub.payment.module.pay.service.union.common.Util;
import com.dicomclub.payment.util.DateUtils;
import com.dicomclub.payment.util.MapUtil;
import com.dicomclub.payment.util.MoneyUtil;
import com.dicomclub.payment.util.WebUtil;
import com.dicomclub.payment.util.httpRequest.HttpConfigStorage;
import com.dicomclub.payment.util.httpRequest.HttpRequestTemplate;
import com.dicomclub.payment.util.httpRequest.UriVariables;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.cert.*;
import java.sql.Timestamp;
import java.util.*;

/**
 * @author ftm
 * @date 2023/2/17 0017 17:48
 */
@Slf4j
@Component
public class UnionPayStrategy extends PayStrategy {


  private HttpRequestTemplate requestTemplate = null;


  /**
   * ????????????
   */
  private static final String TEST_BASE_DOMAIN = "test.95516.com";
  /**
   * ????????????
   */
  private static final String RELEASE_BASE_DOMAIN = "95516.com";
  /**
   * ??????????????????
   */
  private static final String FRONT_TRANS_URL = "https://gateway.%s/gateway/api/frontTransReq.do";
  private static final String BACK_TRANS_URL = "https://gateway.%s/gateway/api/backTransReq.do";
  private static final String SINGLE_QUERY_URL = "https://gateway.%s/gateway/api/queryTrans.do";
  private static final String BATCH_TRANS_URL = "https://gateway.%s/gateway/api/batchTrans.do";
  private static final String FILE_TRANS_URL = "https://filedownload.%s/";
  private static final String APP_TRANS_URL = "https://gateway.%s/gateway/api/appTransReq.do";
  private static final String CARD_TRANS_URL = "https://gateway.%s/gateway/api/cardTransReq.do";


  public UnionPayStrategy(){
      //?????????????????????
      HttpConfigStorage httpConfigStorage = new HttpConfigStorage();
      //???????????????
      httpConfigStorage.setMaxTotal(20);
      //???????????????????????????????????????
      httpConfigStorage.setDefaultMaxPerRoute(10);
      requestTemplate = new HttpRequestTemplate(httpConfigStorage);
  }





  /**
   * ???????????????
   *
   * @param payRequest
   * @return
   */
  @Override
  public PayResponse pay(PayRequest payRequest, PayConfig payConfig) {
    UnionPayConfig unionPayConfig = (UnionPayConfig) payConfig;
//      ??????,????????????????????????
    CertDescriptor certDescriptor = initCertDescriptor(unionPayConfig);


    UnionPayRequest order = (UnionPayRequest) payRequest;
    Map<String, Object> params = this.getCommonParam(unionPayConfig);
    PayChannel type = payRequest.getPayChannel();
//      ??????????????????
    OrderParaStructure.loadParameters(params, UnionPayConstants.param_backUrl, unionPayConfig.getNotifyUrl());
    OrderParaStructure.loadParameters(params, UnionPayConstants.param_backUrl,order.getNotifyUrl());

    //?????????????????????????????????
    if(payRequest.getPayChannel() == PayChannel.UNION_WEB_GATEWAY){
      UnionTransactionType.WEB.convertMap(params);
    }
    if(payRequest.getPayChannel() == PayChannel.UNION_MOBILE_WAP){
      UnionTransactionType.WAP.convertMap(params);
    }

    params.put(UnionPayConstants.param_orderId, order.getOrderNo());

    if (StringUtils.isNotEmpty(order.getAttach())) {
      params.put(UnionPayConstants.param_reqReserved, order.getAttach());
    }
    switch (type) {
      case UNION_MOBILE_WAP:
      case UNION_WEB_GATEWAY:
        //todo PCwap??????????????????????????????.txt
      case UNION_B2B:
        // ?????????(????????????)
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
//        params = preOrderHandler(params, order);
    Map<String, Object> map = setSign(params ,unionPayConfig,certDescriptor);
    String directHtml = buildRequest(map,unionPayConfig);
    UnionPayResponse response = new UnionPayResponse();
    if(payRequest.getPayDataType()!=null&&payRequest.getPayDataType() == PayDataType.PAY_URL){
      response.setPayUrl(getUnionRequestUrl(params, unionPayConfig));
    }else if(payRequest.getPayDataType() == PayDataType.FORM){
      response.setFormContent(buildForm(map,unionPayConfig));
    }else{
      response.setBody(directHtml);
    }
    return response;
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
      throw new PayException("???????????????????????????");
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
   * ????????????
   *
   * @param notifyData
   * @return
   */
  @Override
  public PayResponse asyncNotify(String notifyData,PayConfig payConfig) {
//  ?????????????????????
    HashMap<String, Object> dataMap = MapUtil.form2MapObjectAndURLDecode(notifyData);
//  ??????
    if (null == dataMap || dataMap.get(UnionPayConstants.param_signature) == null) {
      log.debug("???????????????????????????params???" + dataMap);
      throw new PayException("??????????????????!");
    }
    boolean sign = this.signVerify(dataMap, (String) dataMap.get(UnionPayConstants.param_signature), (UnionPayConfig) payConfig);
    if(!sign){
      throw new PayException("????????????????????????!");
    }

    UnionPayResponse payResponse = new UnionPayResponse();
    payResponse.setChannelState(ChannelState.WAITING);
    //????????????
    if (UnionPayConstants.OK_RESP_CODE.equals(dataMap.get(UnionPayConstants.param_respCode))) {
      //???????????????????????????
      payResponse.setChannelState(ChannelState.CONFIRM_SUCCESS);
    }
    else{
//      payResponse.setChannelState(ChannelState.CONFIRM_FAIL);//???????????????????????????????????????????????????????????????????????????
        payResponse.setErrCode((String)dataMap.get(UnionPayConstants.param_respCode));
        payResponse.setErrMsg((String)dataMap.get(UnionPayConstants.param_respMsg));
    }

    if(payResponse.getChannelState() == ChannelState.CONFIRM_SUCCESS){
      return buildPayResponse(dataMap ,payResponse);
    }
//      ?????????????????????
    return payResponse;

  }


  /**
   * ??????????????????
   *
   * @param request
   * @param payConfig
   */
  @Override
  public OrderQueryResponse query(OrderQueryRequest request, PayConfig payConfig) {
      UnionPayConfig unionPayConfig = (UnionPayConfig)payConfig;
      Map<String, Object> params = this.getCommonParam(unionPayConfig);
      UnionTransactionType.QUERY.convertMap(params);
      params.put(UnionPayConstants.param_orderId, request.getOrderNo());
      this.setSign(params,unionPayConfig,initCertDescriptor(unionPayConfig));
      String responseStr = requestTemplate.postForObject(String.format(SINGLE_QUERY_URL, getReqUrl(unionPayConfig.isSandbox())), params, String.class);
      JSONObject response = UriVariables.getParametersToMap(responseStr);
      boolean sign = this.signVerify(response, (String) response.get(UnionPayConstants.param_signature), unionPayConfig);
      if(!sign){
          throw new PayException("failure ????????????????????????");
      }
      ChannelState channelState = ChannelState.WAITING;
      if (UnionPayConstants.OK_RESP_CODE.equals(response.getString(UnionPayConstants.param_respCode))) {
          String origRespCode = response.getString(UnionPayConstants.param_origRespCode);
          if ((UnionPayConstants.OK_RESP_CODE).equals(origRespCode)) {
              //???????????????????????????????????????
              channelState =ChannelState.CONFIRM_SUCCESS;
          }
      }else{
          throw  new PayException(response.getString(UnionPayConstants.param_respCode), response.getString(UnionPayConstants.param_respMsg));
      }
      OrderQueryResponse build = OrderQueryResponse.builder()
              .channelState(channelState)
              .outTradeNo(response.getString(UnionPayConstants.param_queryId))
              .orderNo(response.getString(UnionPayConstants.param_orderId))
              .resultMsg(response.getString(UnionPayConstants.param_origRespMsg))
              .finishTime(response.getString(UnionPayConstants.param_txnTime))
              .build();
      return build;
  }


    private PayResponse buildPayResponse(HashMap<String, Object> dataMap, UnionPayResponse payResponse) {
        payResponse.setOrderAmount(MoneyUtil.Fen2Yuan(Integer.valueOf((String)dataMap.get(UnionPayConstants.param_settleAmt))));
        payResponse.setOrderId((String)dataMap.get(UnionPayConstants.param_orderId));
//    payResponse.setOutTradeNo(response.getTradeNo());
        payResponse.setAttach((String)dataMap.get(UnionPayConstants.param_reqReserved));
        return payResponse;
    }


    /**
   * ?????????????????????????????????????????????encoding?????????????????????????????????
   *
   * @return ??????????????????
   */
  private Map<String, Object> getCommonParam(UnionPayConfig unionPayConfig) {
    Map<String, Object> params = new TreeMap<>();
    UnionPayConfig  configStorage = unionPayConfig;
    //??????????????????
    params.put(UnionPayConstants.param_version, configStorage.getVersion());
    //????????????
    params.put(UnionPayConstants.param_encoding, unionPayConfig.getInputCharset().toUpperCase());
    //????????????
    params.put(UnionPayConstants.param_merId, unionPayConfig.getPid());

    //??????????????????
    params.put(UnionPayConstants.param_txnTime, DateUtils.formatDate(new Date(), DateUtils.YYYYMMDDHHMMSS));
    //??????????????????
    params.put(UnionPayConstants.param_backUrl, unionPayConfig.getNotifyUrl());

    //????????????
    params.put(UnionPayConstants.param_currencyCode, "156");
    //??????????????????????????????0 ??????????????????0?????????????????? 1??? ???????????? 2??????????????????
    params.put(UnionPayConstants.param_accessType, configStorage.getAccessType());
    return params;
  }



  /**
   * ?????????????????????
   * ??????????????????????????????????????????????????????????????????????????????????????????????????? ??????????????????????????????????????????????????????????????????????????????5?????????????????????????????????????????????
   * ?????????????????????????????????????????????15?????????
   * ???????????????????????????????????????origRespCode??????A6??????00??????????????????????????????
   *
   * @param expirationTime ????????????
   * @return ????????????????????????
   */
  private String getPayTimeout(Date expirationTime) {
    //
    if (null != expirationTime) {
      return DateUtils.formatDate(expirationTime, DateUtils.YYYYMMDDHHMMSS);
    }
    return DateUtils.formatDate(new Timestamp(System.currentTimeMillis() + 30 * 60 * 1000), DateUtils.YYYYMMDDHHMMSS);
  }



  /**
   * ?????????????????????
   *
   * @param parameters ????????????
   * @return ????????????
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
        throw new PayException("sign fail ????????????????????????");
    }


    return parameters;
  }

  /**
   * ??????????????????????????????Html??????
   *
   * @param orderInfo ???????????????????????????
   * @return ?????????????????????Html????????????????????????, ?????????PC???
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
   * ???????????????????????????
   *
   */
  public String getReqUrl(Boolean isSandBox) {
    return (isSandBox ? TEST_BASE_DOMAIN : RELEASE_BASE_DOMAIN);
  }


  /**
   * ????????????
   *
   * @param params ?????????
   * @param sign   ????????????
   * @return ???????????? true??????
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
   * ?????????????????????X509Certificate??????.
   *
   * @param x509CertString ?????????
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
      throw new PayException("??????????????????"+ "gen certificate error:" + e.getLocalizedMessage());
    }
    return x509Cert;
  }


  /**
   * ???????????????
   *
   * @param cert ?????????????????????
   */
  private X509Certificate verifyCertificate(CertDescriptor certDescriptor,X509Certificate cert) {
    try {
      cert.checkValidity();//???????????????
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
