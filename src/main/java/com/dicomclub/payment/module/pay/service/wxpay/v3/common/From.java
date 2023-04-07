package com.dicomclub.payment.module.pay.service.wxpay.v3.common;

/**
 * 退款出资的账户类型及金额信息
 * @author ftm
 * @date 2023/3/21 0021 14:31
 */
public class From {

    /**
     * 出资账户类型
     * 下面枚举值多选一。
     * 枚举值：
     * AVAILABLE : 可用余额
     * UNAVAILABLE : 不可用余额
     */
    private String account;
    /**
     * 对应账户出资金额
     */
    private Integer amount;

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public Integer getAmount() {
        return amount;
    }

    public void setAmount(Integer amount) {
        this.amount = amount;
    }
}
