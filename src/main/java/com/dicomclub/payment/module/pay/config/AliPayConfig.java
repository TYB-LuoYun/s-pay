package com.dicomclub.payment.module.pay.config;

import com.dicomclub.payment.exception.PayException;
import com.dicomclub.payment.module.pay.model.alipay.CertEnvironment;
import com.dicomclub.payment.util.httpRequest.CertStore;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

/**
 */
@Data
@NoArgsConstructor
public class AliPayConfig extends PayConfig {
    /** 网关地址 */
    public static String PROD_SERVER_URL = "https://openapi.alipay.com/gateway.do";
    public static String SANDBOX_SERVER_URL = "https://openapi.alipaydev.com/gateway.do";

    public static String PROD_OAUTH_URL = "https://openauth.alipay.com/oauth2/publicAppAuthorize.htm?app_id=%s&scope=auth_base&state=&redirect_uri=%s";
    public static String SANDBOX_OAUTH_URL = "https://openauth.alipaydev.com/oauth2/publicAppAuthorize.htm?app_id=%s&scope=auth_base&state=&redirect_uri=%s";

    /** isv获取授权商户URL地址 **/
    public static String PROD_APP_TO_APP_AUTH_URL = "https://openauth.alipay.com/oauth2/appToAppAuth.htm?app_id=%s&redirect_uri=%s&state=%s";
    public static String SANDBOX_APP_TO_APP_AUTH_URL = "https://openauth.alipaydev.com/oauth2/appToAppAuth.htm?app_id=%s&redirect_uri=%s&state=%s";


    public static String FORMAT = "json";

    public static String CHARSET = "UTF-8";

    /**
     * appId
     */
    private String appId;
    /**
     * 商户私钥
     */
    private String privateKey;
    /**
     * 支付宝公钥
     */
    private String aliPayPublicKey;

    public void check() {
        Objects.requireNonNull(appId, "config param 'appId' is null.");
        Objects.requireNonNull(privateKey, "config param 'privateKey' is null.");
        Objects.requireNonNull(aliPayPublicKey, "config param 'aliPayPublicKey' is null.");
        if (appId.length() > 32) {
            throw new IllegalArgumentException("config param 'appId' is incorrect: size exceeds 32.");
        }
    }

    /** 是否使用证书方式 **/
    private Boolean useCert;

    /** app应用公钥 证书 -- 在开放平台上传 CSR 文件后可以获取 CA 机构颁发的应用证书文件（.crt），其中包含了组织/公司名称、应用公钥、证书有效期等内容，一般有效期为 5 年。 **/
    private Object appPublicCert;

    /** 支付宝公钥证书（.crt格式） -- 用来验证支付宝消息，包含了支付宝公钥、支付宝公司名称、证书有效期等内容，一般有效期为 5 年 **/
    private Object aliPayPublicCert;

    /** 支付宝根证书 -- 用来验证支付宝消息，包含了根 CA 名称、根 CA 的公钥、证书有效期等内容 **/
    private Object aliPayRootCert;

    /**
     * 证书信息
     */
    private CertEnvironment certEnvironment;



    /**
     * 是否为服务商模式, 默认为false
     */
    private boolean isPartner = false;

    /**
     * 证书存储类型
     */
    private CertStore certStoreType;



    public CertEnvironment getCertEnvironment() {
        loadCertEnvironment();
        return certEnvironment;
    }

    /**
     * 初始化证书信息
     */
    public void loadCertEnvironment() {
        if (!getUseCert() || null != this.certEnvironment) {
            return;
        }
        try (InputStream merchantCertStream = certStoreType.getInputStream(appPublicCert);
             InputStream aliPayCertStream = certStoreType.getInputStream(aliPayPublicCert);
             InputStream aliPayRootCertStream = certStoreType.getInputStream(aliPayRootCert)) {
             this.certEnvironment = new CertEnvironment(merchantCertStream, aliPayCertStream, aliPayRootCertStream);
        }
        catch(IOException e) {
            throw  new PayException("读取证书异常"+ e.getMessage());
        }
    }

}
