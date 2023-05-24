package com.dicomclub.payment.module.pay.config;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.dicomclub.payment.exception.PayException;
import com.dicomclub.payment.module.pay.common.AesUtils;
import com.dicomclub.payment.module.pay.config.PayConfig;
import com.dicomclub.payment.module.pay.model.wxpay.WxPlatCert;
import com.dicomclub.payment.module.pay.service.wxpay.config.CertEnvironment;
import com.dicomclub.payment.module.pay.service.wxpay.v3.common.AntCertificationUtil;
import com.dicomclub.payment.module.pay.service.wxpay.v3.common.StringUtils;
import com.dicomclub.payment.module.pay.service.wxpay.v3.common.WxConst;
import com.dicomclub.payment.module.pay.service.wxpay.v3.common.WxTransactionType;
import com.dicomclub.payment.module.pay.service.wxpay.v3.service.WxPayAssistService;
import com.dicomclub.payment.util.DateUtils;
import com.dicomclub.payment.util.SpringContextUtil;
import com.dicomclub.payment.util.httpRequest.CertStoreType;
import com.dicomclub.payment.util.httpRequest.MethodType;
import com.dicomclub.payment.util.httpRequest.ResponseEntity;
import com.dicomclub.payment.util.httpRequest.UriVariables;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.http.HttpEntity;
import org.apache.http.ssl.SSLContexts;
import org.apache.tomcat.util.http.fileupload.IOUtils;

import javax.net.ssl.SSLContext;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.util.Date;

import static com.dicomclub.payment.module.pay.service.wxpay.v3.common.WxConst.SANDBOXNEW;

/**
 * 2017-07-02 13:58
 */
@Data
@NoArgsConstructor
public class WxPayConfig extends PayConfig {

    /**
     * 公众号appId
     */
    private String appId;

    /**
     * 公众号appSecret
     */
    private String appSecret;

    /**
     * 小程序appId
     */
    private String miniAppId;

    /**
     * app应用appid
     */
    private String appAppId;

    /**
     * 商户号
     */
    private String mchId;

    /**
     * 商户密钥
     */
    private String mchKey;

    /**
     * 商户证书路径
     */
    private String keyPath;

    /**
     * 证书内容
     */
    private SSLContext sslContext;



//==================================================下面是V3升级版参数===================================


//    /**
//     * 应用私钥，rsa_private pkcs8格式 生成签名时使用,用下面的替换了哈
//     */
//    private String keyPrivate;


    /**
     * 商户私钥
     */
    private String privateKey;



    /**
     * 微信支付分配的子商户号，开发者模式下必填 合作者id
     */
    private String subMchId;

    /**
     * 子商户应用ID, 非必填
     * 子商户申请的公众号appid。
     * 若sub_openid有传的情况下，sub_appid必填，且sub_appid需与sub_openid对应
     * 示例值：wxd678efh567hg6999
     */
    private String subAppId;


    /**
     * 是否为服务商模式, 默认为false
     */
    private boolean partner = false;


    /**
     * 证书信息
     */
    private volatile CertEnvironment certEnvironment;


    /**
     * 商户API证书
     * 包含商户的商户号、公司名称、公钥信息
     * 详情 https://pay.weixin.qq.com/wiki/doc/apiv3/wechatpay/wechatpay3_1.shtml
     */
    private Object apiClientKeyP12;

    /**
     * 证书存储类型
     */
    private CertStoreType certStoreType;


    /**
     * 平台证书
     * @return
     */
    private WxPlatCert wxPlatCert;


    /**
     * 初始化平台证书
     * @return
     */
    public WxPlatCert initLatestWxPlatCert(){
        if(wxPlatCert == null || wxPlatCert.getExpireTime().getTime()-new Date().getTime() <= 5000){
            wxPlatCert = getPlatCertificate(this);
        }
        return wxPlatCert;
    }


    public WxPlatCert getWxPlatCert(){
        return wxPlatCert;
    }


    public WxPlatCert  getPlatCertificate(WxPayConfig payConfig){
        JSONObject jsonObject = SpringContextUtil.getBean(WxPayAssistService.class).doExecute(null, WxTransactionType.Certificate, payConfig);
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
        WxPlatCert wxPlatCert = new WxPlatCert();
        wxPlatCert.setExpireTime(DateUtils.parseDate(encryptObject.getString("expire_time"), DateUtils.YYYY_MM_DD_T_HH_MM_SS_XX));
        //        return certificate.getSerialNumber().toString(16).toUpperCase();
        wxPlatCert.setSerialNumber(serialNo);
        wxPlatCert.setCertificate(certificate);
        return wxPlatCert;
    }



    public CertEnvironment getCertEnvironment() {
        loadCertEnvironment();
        return certEnvironment;
    }
    public void setCertEnvironment(CertEnvironment certEnvironment) {
        this.certEnvironment = certEnvironment;
    }
    /**
     * 初始化证书信息
     */
    public void loadCertEnvironment() {
        if (null != this.certEnvironment) {
            return;
        }
        try (InputStream apiKeyCert = certStoreType.getInputStream(getApiClientKeyP12())) {
            this.certEnvironment = AntCertificationUtil.initCertification(apiKeyCert, WxConst.CERT_ALIAS, getMchId());
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new PayException("读取证书异常"+e.getMessage());
        }
    }

    /**
     * 初始化证书
     * @return
     */
    public SSLContext initSSLContext() {
        FileInputStream inputStream = null;
        try {
            inputStream = new FileInputStream(new File(this.keyPath));
        } catch (IOException e) {
            throw new RuntimeException("读取微信商户证书文件出错", e);
        }

        try {
            KeyStore keystore = KeyStore.getInstance("PKCS12");
            char[] partnerId2charArray = mchId.toCharArray();
            keystore.load(inputStream, partnerId2charArray);
            this.sslContext = SSLContexts.custom().loadKeyMaterial(keystore, partnerId2charArray).build();
            return this.sslContext;
        } catch (Exception e) {
            throw new RuntimeException("证书文件有问题，请核实！", e);
        } finally {
            IOUtils.closeQuietly(inputStream);
        }
    }

    public void setKeyPath(String object){
        keyPath = object;
        if(apiClientKeyP12 == null){
            apiClientKeyP12 = object;
        }
    }


    /**
     * 为商户平台设置的密钥key
     *
     * @return 微信v3密钥
     */
    public String getV3ApiKey() {
        return getPrivateKey();
    }

    public String getPrivateKey() {
        return privateKey;
    }

    public String getApiV3Key() {
        return  getPrivateKey();
    }


    public Object getApiClientKeyP12(){
        if(apiClientKeyP12 == null){
            return keyPath;
        }
        return apiClientKeyP12;
    }


}
