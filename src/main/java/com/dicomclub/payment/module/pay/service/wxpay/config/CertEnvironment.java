package com.dicomclub.payment.module.pay.service.wxpay.config;

import java.security.PrivateKey;
import java.security.PublicKey;

/**
 * @author ftm
 * @date 2023/3/3 0003 13:14
 */
public class CertEnvironment {
    /**
     * 存放私钥
     */
    private PrivateKey privateKey;

    /**
     * 存放公钥
     */
    private PublicKey publicKey;

    /**
     * 公钥序列
     */
    private String serialNumber;






    public CertEnvironment() {
    }

    public CertEnvironment(PrivateKey privateKey, PublicKey publicKey, String serialNumber) {
        this.privateKey = privateKey;
        this.publicKey = publicKey;
        this.serialNumber = serialNumber;
    }

    public PrivateKey getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(PrivateKey privateKey) {
        this.privateKey = privateKey;
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(PublicKey publicKey) {
        this.publicKey = publicKey;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }
}
