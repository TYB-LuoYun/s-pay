package com.dicomclub.payment.module.pay.model.wxpay;

import com.dicomclub.payment.module.pay.model.PayRequest;
import lombok.Data;

/**
 * @author ftm
 * @date 2023/2/16 0016 13:29
 */
@Data
public class WxPayRequest extends PayRequest {
    /**
     * 微信openid, 仅微信公众号/小程序支付时需要
     */
    private String openid;


    /**
     * 客户端访问Ip  外部H5支付时必传，需要真实Ip
     * 20191015测试，微信h5支付已不需要真实的ip
     */
    private String spbillCreateIp;
}
