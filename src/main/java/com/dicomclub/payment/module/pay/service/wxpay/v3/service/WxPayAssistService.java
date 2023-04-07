package com.dicomclub.payment.module.pay.service.wxpay.v3.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.dicomclub.payment.exception.PayErrorException;
import com.dicomclub.payment.exception.PayException;
import com.dicomclub.payment.exception.WxPayError;
import com.dicomclub.payment.module.pay.common.AesUtils;
import com.dicomclub.payment.module.pay.common.TransactionType;
import com.dicomclub.payment.module.pay.config.WxPayConfig;
import com.dicomclub.payment.module.pay.service.union.common.sign.SignTextUtils;
import com.dicomclub.payment.module.pay.service.union.common.sign.encrypt.RSA2;
import com.dicomclub.payment.module.pay.service.wxpay.v3.common.AntCertificationUtil;
import com.dicomclub.payment.module.pay.service.wxpay.v3.common.StringUtils;
import com.dicomclub.payment.module.pay.service.wxpay.v3.common.WxConst;
import com.dicomclub.payment.module.pay.service.wxpay.v3.common.WxTransactionType;
import com.dicomclub.payment.module.pay.service.wxpay.v3.model.OriginNotifyResponse;
import com.dicomclub.payment.module.pay.service.wxpay.v3.model.WxPayOrderNotifyV3Result;
import com.dicomclub.payment.module.pay.service.wxpay.v3.model.WxPayRefundNotifyV3Result;
import com.dicomclub.payment.util.DateUtils;
import com.dicomclub.payment.util.httpRequest.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.message.BasicHeader;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.util.Map;
import java.util.Objects;

import static com.dicomclub.payment.module.pay.service.wxpay.v3.common.WxConst.SANDBOXNEW;
import static org.apache.http.entity.ContentType.APPLICATION_JSON;

/**
 * @author ftm
 * @date 2023/3/21 0021 9:35
 * 微信支付辅助服务
 */

@Slf4j
@Component
public class WxPayAssistService {

    private HttpRequestTemplate requestTemplate = null;


    public WxPayAssistService(){
        //请求连接池配置
        HttpConfigStorage httpConfigStorage = new HttpConfigStorage();
        //最大连接数
        httpConfigStorage.setMaxTotal(20);
        //默认的每个路由的最大连接数
        httpConfigStorage.setDefaultMaxPerRoute(10);
        requestTemplate = new HttpRequestTemplate(httpConfigStorage);
    }


    /**
     * 当缓存中平台证书不存在事进行刷新重新获取平台证书
     * 调用/v3/certificates
     */
    public void refreshCertificate(WxPayConfig payConfigStorage) {
        JSONObject responseEntity = doExecute("", WxTransactionType.CERT,payConfigStorage);

        if (null == responseEntity) {
            throw new PayErrorException(new WxPayError(WxConst.FAILURE, "获取证书失败"));
        }
        JSONArray certificates = responseEntity.getJSONArray("data");
        if (null == certificates) {
            return;
        }
        for (int i = 0; i < certificates.size(); i++) {
            JSONObject certificate = certificates.getJSONObject(i);
            JSONObject encryptCertificate = certificate.getJSONObject("encrypt_certificate");
            String associatedData = encryptCertificate.getString("associated_data");
            String nonce = encryptCertificate.getString("nonce");
            String ciphertext = encryptCertificate.getString("ciphertext");
            String publicKey = AntCertificationUtil.decryptToString(associatedData, nonce, ciphertext, payConfigStorage.getV3ApiKey(), payConfigStorage.getInputCharset());
            ByteArrayInputStream inputStream = new ByteArrayInputStream(publicKey.getBytes(StandardCharsets.UTF_8));
            AntCertificationUtil.loadCertificate(certificate.getString("serial_no"), inputStream);
        }

    }



    /**
     * 通过证书序列获取平台证书
     *
     * @param serialNo 证书序列
     * @return 平台证书
     */
    public Certificate getCertificate(String serialNo,WxPayConfig payConfig) {
        Certificate certificate = AntCertificationUtil.getCertificate(serialNo);
        if (null == certificate) {
            refreshCertificate(payConfig);
            certificate = AntCertificationUtil.getCertificate(serialNo);
        }
        return certificate;
    }


    /**
     * 发起请求
     *
     * @param body            请求内容
     * @param transactionType 交易类型
     * @param uriVariables    用于匹配表达式
     * @return 响应内容体
     */
    public JSONObject doExecute(String body, TransactionType transactionType, WxPayConfig payConfigStorage,Object... uriVariables) {
        final ResponseEntity<JSONObject> responseEntity = doExecuteEntity(body, transactionType,payConfigStorage, uriVariables);
        int statusCode = responseEntity.getStatusCode();
        JSONObject responseBody = responseEntity.getBody();
        if (statusCode >= 400) {
            throw new PayErrorException(new WxPayError(responseBody.getString(WxConst.CODE), responseBody.getString(WxConst.MESSAGE), responseBody.toJSONString()));
        }
        return responseBody;
    }


    /**
     * 发起请求
     *
     * @param parameters      支付参数
     * @param transactionType 交易类型
     * @return 响应内容体
     */
    public JSONObject doExecute(Map<String, Object> parameters, TransactionType transactionType,WxPayConfig payConfigStorage) {
//        String requestBody = JSON.toJSONString(parameters, SerializerFeature.WriteMapNullValue);
        String requestBody = JSON.toJSONString(parameters);
        return doExecute(requestBody, transactionType,payConfigStorage);
    }



    /**
     * 发起请求
     *
     * @param body            请求内容
     * @param transactionType 交易类型
     * @param uriVariables    用于匹配表达式
     * @return 响应内容体
     */
    public ResponseEntity<JSONObject> doExecuteEntity(String body, TransactionType transactionType,WxPayConfig payConfigStorage, Object... uriVariables) {
        String reqUrl = UriVariables.getUri(getReqUrl(transactionType,payConfigStorage), uriVariables);
        MethodType method = MethodType.valueOf(transactionType.getMethod());
//        log.debug("请求路径:"+reqUrl);
        if (MethodType.GET == method && StringUtils.isNotEmpty(body)) {
            reqUrl += UriVariables.QUESTION.concat(body);
            body = "";
        }
        HttpEntity entity = buildHttpEntity(reqUrl, body, transactionType.getMethod(),payConfigStorage);
        ResponseEntity<JSONObject> responseEntity = requestTemplate.doExecuteEntity(reqUrl, entity, JSONObject.class, method);
        return responseEntity;
    }

    /**
     * 根据交易类型获取url
     *
     * @param transactionType 交易类型
     * @return 请求url
     */
    public String getReqUrl(TransactionType transactionType,WxPayConfig payConfigStorage) {
        String type = transactionType.getType();
        String partnerStr = "";
        if (payConfigStorage.isPartner()) {
            partnerStr = "/partner";
        }
        type = type.replace("{partner}", partnerStr);
        return WxConst.URI + (payConfigStorage.isSandbox() ? SANDBOXNEW : "") + type;
    }



    /**
     * 构建请求实体
     * 这里也做签名处理
     *
     * @param url   url
     * @param body   请求内容体
     * @param method 请求方法
     * @return 请求实体
     */
    public HttpEntity buildHttpEntity(String url, String body, String method,WxPayConfig payConfigStorage) {
        String nonceStr = SignTextUtils.randomStr();
        long timestamp = DateUtils.toEpochSecond();
        String canonicalUrl = UriVariables.getCanonicalUrl(url);
        //签名信息
        String signText = StringUtils.joining("\n", method, canonicalUrl, String.valueOf(timestamp), nonceStr, body);
        String sign =  createSign(signText, payConfigStorage.getInputCharset(),payConfigStorage);
        String serialNumber = payConfigStorage.getCertEnvironment().getSerialNumber();
        // 生成token
        String token = String.format(WxConst.TOKEN_PATTERN, payConfigStorage.getMchId(), nonceStr, timestamp, serialNumber, sign);
        HttpStringEntity entity = new HttpStringEntity(body, ContentType.APPLICATION_JSON);
        entity.addHeader(new BasicHeader("Authorization", WxConst.SCHEMA.concat(token)));
        entity.addHeader(new BasicHeader("User-Agent", "Pay-Java-Parent"));
        entity.addHeader(new BasicHeader("Accept", APPLICATION_JSON.getMimeType()));
        return entity;
    }


    public String createSign(String content, String characterEncoding ,WxPayConfig payConfigStorage) {
        PrivateKey privateKey = payConfigStorage.getCertEnvironment().getPrivateKey();
        return RSA2.sign(content, privateKey, characterEncoding);
    }



    public WxPayOrderNotifyV3Result parseOrderNotifyV3Result(String notifyData, WxPayConfig wxPayConfig) {

            OriginNotifyResponse response =  JSONObject.parseObject(notifyData, OriginNotifyResponse.class);
            OriginNotifyResponse.Resource resource = response.getResource();
            String cipherText = resource.getCiphertext();
            String associatedData = resource.getAssociatedData();
            String nonce = resource.getNonce();
            String apiV3Key = wxPayConfig.getApiV3Key();

            try {
                String result = AesUtils.decryptToString(associatedData, nonce, cipherText, apiV3Key);
                WxPayOrderNotifyV3Result.DecryptNotifyResult decryptNotifyResult = (WxPayOrderNotifyV3Result.DecryptNotifyResult)JSONObject.parseObject( result, WxPayOrderNotifyV3Result.DecryptNotifyResult.class);
                WxPayOrderNotifyV3Result notifyResult = new WxPayOrderNotifyV3Result();
                notifyResult.setRawData(response);
                notifyResult.setResult(decryptNotifyResult);
                return notifyResult;
            } catch (IOException | GeneralSecurityException var12) {
                throw new PayException("微信解析报文异常！", var12);
            }
    }


    public WxPayRefundNotifyV3Result.DecryptNotifyResult parseRefundNotifyV3Result(String notifyData, WxPayConfig wxPayConfig) {
        OriginNotifyResponse response = JSON.parseObject(notifyData, OriginNotifyResponse.class);
        OriginNotifyResponse.Resource resource = response.getResource();
        String cipherText = resource.getCiphertext();
        String associatedData = resource.getAssociatedData();
        String nonce = resource.getNonce();
        String apiV3Key =wxPayConfig.getApiV3Key();

        try {
            String result = AesUtils.decryptToString(associatedData, nonce, cipherText, apiV3Key);
            WxPayRefundNotifyV3Result.DecryptNotifyResult decryptNotifyResult =  JSON.parseObject(result,  WxPayRefundNotifyV3Result.DecryptNotifyResult.class);
            WxPayRefundNotifyV3Result notifyResult = new WxPayRefundNotifyV3Result();
            notifyResult.setRawData(response);
            notifyResult.setResult(decryptNotifyResult);
            return notifyResult.getResult();
        } catch (IOException | GeneralSecurityException var12) {
            throw new PayException("解析微信退款报文异常！", var12);
        }

    }
}
