package com.dicomclub.payment.module.pay.enums;

/**
 * @author ftm
 * @date 2023/4/19 0019 13:00
 */
public enum ReceiverType {
    /**
     * 商户号
     */
    MERCHANT_ID,
    /**
     * 个人openid（由父商户APPID转换得到）
     */
    PERSONAL_OPENID,
    /**
     * 个人sub_openid（由子商户APPID转换得到），服务商模式
     */
    PERSONAL_SUB_OPENID
}
