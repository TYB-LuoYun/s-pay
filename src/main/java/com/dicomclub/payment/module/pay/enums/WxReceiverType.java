package com.dicomclub.payment.module.pay.enums;

import com.dicomclub.payment.exception.PayException;

/**
 * @author ftm
 * @date 2023/4/19 0019 13:00
 */
public enum WxReceiverType {
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
    PERSONAL_SUB_OPENID;

    public static WxReceiverType getValid(String accountType) {
        for(WxReceiverType item: WxReceiverType.values()){
            if(item.name().equals(accountType)){
                return item;
            }
        }
        throw new PayException("不支持的分账接受者类型");
    }
}
