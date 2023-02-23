package com.dicomclub.payment.module.pay.enums;

/**
 * 支付类型
 * Created by null on 2019/9/19.
 */
public enum PayType {

    ALIPAY("alipay", "支付宝"),

    WX("wxpay", "微信"),

    UNION("union","银联")
    ;

    private String code;

    private String name;

    PayType(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }
}
