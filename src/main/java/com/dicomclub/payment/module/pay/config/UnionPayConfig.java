package com.dicomclub.payment.module.pay.config;

import com.dicomclub.payment.util.httpRequest.CertStoreType;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author ftm
 * @date 2023/2/17 0017 17:49
 */
public class UnionPayConfig extends PayConfig {

    /**
     * 应用私钥，rsa_private pkcs8格式 生成签名时使用
     */
    private String keyPrivate;

    /**
     * 商户号
     */
    private String merId;

    /**
     * 商户收款账号
     */
    private String seller;

    private String version = "5.1.0";
    /**
     * 0：普通商户直连接入
     * 1： 收单机构
     * 2：平台类商户接入
     */
    private String accessType = "0";


    /**
     * 应用私钥证书
     */
    private Object keyPrivateCert;
    /**
     * 应用私钥证书，rsa_private pkcs8格式 生成签名时使用
     */
    private String keyPrivateCertPwd;

    /**
     * 中级证书
     */
    private Object acpMiddleCert;
    /**
     * 根证书
     */
    private Object acpRootCert;

    /**
     * 证书存储类型
     */
    private CertStoreType certStoreType;


    /**
     * 字符类型
     */
    private String inputCharset;

    /**
     * 签名加密类型
     */
    private String signType;


    public String getSignType() {
        return signType;
    }

    public void setSignType(String signType) {
        this.signType = signType;
    }


    /**
     * 是否为证书签名
     */
    private boolean certSign = false;


    public void setCertSign(boolean certSign) {
        this.certSign = certSign;
    }

    public String getKeyPrivate() {
        return keyPrivate;
    }

    /**
     * 设置私钥证书
     *
     * @param certificate 私钥证书地址 或者证书内容字符串
     *                    私钥证书密码 {@link #setKeyPrivateCertPwd(String)}
     */
    public void setKeyPrivateCert(String certificate) {
        this.setKeyPrivate(certificate);
        this.keyPrivateCert = certificate;
    }

    private void setKeyPrivate(String certificate) {
        keyPrivate = certificate;
    }

    /**
     * 设置私钥证书
     *
     * @param keyPrivateCert 私钥证书信息流
     *                       私钥证书密码 {@link #setKeyPrivateCertPwd(String)}
     */
    public void setKeyPrivateCert(InputStream keyPrivateCert) {
        this.keyPrivateCert = keyPrivateCert;
    }

    public InputStream getKeyPrivateCertInputStream() throws IOException {
        return certStoreType.getInputStream(keyPrivateCert);
    }

    /**
     * 设置中级证书
     *
     * @param acpMiddleCert 证书信息或者证书路径
     */
    public void setAcpMiddleCert(String acpMiddleCert) {
        this.acpMiddleCert = acpMiddleCert;
    }

    /**
     * 设置中级证书
     *
     * @param acpMiddleCert 证书文件
     */
    public void setAcpMiddleCert(InputStream acpMiddleCert) {
        this.acpMiddleCert = acpMiddleCert;
    }

    /**
     * 设置根证书
     *
     * @param acpRootCert 证书路径或者证书信息字符串
     */
    public void setAcpRootCert(String acpRootCert) {
        this.acpRootCert = acpRootCert;
    }

    /**
     * 设置根证书
     *
     * @param acpRootCert 证书文件流
     */
    public void setAcpRootCert(InputStream acpRootCert) {
        this.acpRootCert = acpRootCert;
    }

    public String getAcpMiddleCert() {
        return (String) acpMiddleCert;
    }

    public String getAcpRootCert() {
        return (String) acpRootCert;
    }

    public InputStream getAcpMiddleCertInputStream() throws IOException {
        return certStoreType.getInputStream(acpMiddleCert);
    }

    public InputStream getAcpRootCertInputStream() throws IOException {
        return certStoreType.getInputStream(acpRootCert);
    }

    /**
     * 获取私钥证书密码
     *
     * @return 私钥证书密码
     */
    public String getKeyPrivateCertPwd() {
        return keyPrivateCertPwd;
    }

    public void setKeyPrivateCertPwd(String keyPrivateCertPwd) {
        this.keyPrivateCertPwd = keyPrivateCertPwd;
    }





    /**
     * @return 合作者id
     * @see #getPid()
     */
    @Deprecated
    public String getPartner() {
        return merId;
    }


    /**
     * 设置合作者id
     *
     * @param partner 合作者id
     * @see #setPid(String)
     */
    @Deprecated
    public void setPartner(String partner) {
        this.merId = partner;
    }

    public String getPid() {
        return merId;
    }

    public void setPid(String pid) {
        this.merId = pid;
    }

    public String getSeller() {
        return seller;
    }

    public void setSeller(String seller) {
        this.seller = seller;
    }

    public String getMerId() {
        return merId;
    }

    public void setMerId(String merId) {
        this.merId = merId;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getAccessType() {
        return accessType;
    }

    public void setAccessType(String accessType) {
        this.accessType = accessType;
    }

    /**
     * 证书存储类型
     *
     * @return 证书存储类型
     */
    public CertStoreType getCertStoreType() {
        return certStoreType;
    }

    public void setCertStoreType(CertStoreType certStoreType) {
        this.certStoreType = certStoreType;
    }

    public void setInputCharset(String inpustCharset) {
        this.inputCharset = inpustCharset;
    }

    public String getInputCharset() {
        return this.inputCharset;
    }
}
