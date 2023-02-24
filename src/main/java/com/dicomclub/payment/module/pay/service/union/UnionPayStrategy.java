package com.dicomclub.payment.module.pay.service.union;

import com.dicomclub.payment.exception.PayException;
import com.dicomclub.payment.module.pay.config.PayConfig;
import com.dicomclub.payment.module.pay.config.UnionPayConfig;
import com.dicomclub.payment.module.pay.constants.UnionPayConstants;
import com.dicomclub.payment.module.pay.enums.PayChannel;
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
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author ftm
 * @date 2023/2/17 0017 17:48
 */
@Slf4j
@Component
public class UnionPayStrategy extends PayStrategy {


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








  /**
   * 订单预支付
   *
   * @param payRequest
   * @return
   */
  @Override
  public PayResponse pay(PayRequest payRequest, PayConfig payConfig) {
    UnionPayConfig unionPayConfig = (UnionPayConfig) payConfig;
//      证书
    CertDescriptor certDescriptor = null;
    try {
      certDescriptor = new CertDescriptor();
      certDescriptor.initPrivateSignCert(unionPayConfig.getKeyPrivateCertInputStream(), unionPayConfig.getKeyPrivateCertPwd(), "PKCS12");
      certDescriptor.initPublicCert(unionPayConfig.getAcpMiddleCertInputStream());
      certDescriptor.initRootCert(unionPayConfig.getAcpRootCertInputStream());
    }
    catch (IOException e) {
      log.error("", e);
    }

    UnionPayRequest order = (UnionPayRequest) payRequest;
    Map<String, Object> params = this.getCommonParam(unionPayConfig);
    PayChannel type = payRequest.getPayChannel();
//      后台通知地址
    OrderParaStructure.loadParameters(params, UnionPayConstants.param_backUrl, unionPayConfig.getNotifyUrl());
    OrderParaStructure.loadParameters(params, UnionPayConstants.param_backUrl,order.getNotifyUrl());

    //设置交易类型相关的参数
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
        //todo PCwap网关跳转支付特殊用法.txt
      case UNION_B2B:
        // 总金额(单位是分)
        params.put(UnionPayConstants.param_txnAmt,  Util.conversionCentAmount(order.getOrderAmount()));
        params.put("orderDesc", order.getOrderName());
        params.put(UnionPayConstants.param_payTimeout, getPayTimeout(order.getExpirationTime()));

        params.put(UnionPayConstants.param_frontUrl, unionPayConfig.getReturnUrl());
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
    params.putAll(order.getAttrs());
//        params = preOrderHandler(params, order);
    Map<String, Object> map = setSign(params ,unionPayConfig,certDescriptor);
    String directHtml = buildRequest(map,unionPayConfig);
    UnionPayResponse response = new UnionPayResponse();
    response.setBody(directHtml);
    return response;
  }

  /**
   * 异步通知
   *
   * @param notifyData
   * @return
   */
  @Override
  public PayResponse asyncNotify(String notifyData,PayConfig payConfig) {
    return null;
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
    sf.append("<html><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=" + unionPayConfig.getInputCharset()  + "\"/></head><body>");
    sf.append("<form id = \"pay_form\" action=\"" + getReqUrl(unionPayConfig.isSandbox()) + "\" method=\"post\">");
    if (null != orderInfo && 0 != orderInfo.size()) {
      for (Map.Entry<String, Object> entry : orderInfo.entrySet()) {
        String key = entry.getKey();
        Object value = entry.getValue();
        sf.append("<input type=\"hidden\" name=\"" + key + "\" id=\"" + key + "\" value=\"" + value + "\"/>");
      }
    }
    sf.append("</form>");
    sf.append("</body>");
    sf.append("<script type=\"text/javascript\">");
    sf.append("document.all.pay_form.submit();");
    sf.append("</script>");
    sf.append("</html>");
    return sf.toString();
  }



  /**
   * 获取支付请求地址
   *
   * @return 请求地址
   */
  public String getReqUrl(Boolean isSandBox) {
    return (isSandBox ? TEST_BASE_DOMAIN : RELEASE_BASE_DOMAIN);
  }




}
