package com.dicomclub.payment.module.pay.model.wxpay;

import lombok.Data;

import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Date;

/**
 * @author ftm
 * @date 2023/4/19 0019 12:33
 */
@Data
public class WxPlatCert {
    private Date expireTime;

    private Certificate certificate;


    private String serialNumber;
}
