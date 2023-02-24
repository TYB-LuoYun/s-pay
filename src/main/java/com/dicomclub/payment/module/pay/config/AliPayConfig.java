package com.dicomclub.payment.module.pay.config;

import lombok.Data;
import lombok.NoArgsConstructor;

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

    /** app 证书 **/
    private String appPublicCert;

    /** 支付宝公钥证书（.crt格式） **/
    private String aliPayPublicCert;

    /** 支付宝根证书 **/
    private String aliPayRootCert;
}
