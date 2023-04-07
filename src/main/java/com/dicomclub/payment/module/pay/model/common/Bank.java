package com.dicomclub.payment.module.pay.model.common;

/**
 * @author ftm
 * @date 2023/3/27 0027 11:36
 */
public interface Bank {
    /**
     * 获取银行的代码
     * @return 银行的代码
     */
    String getCode();

    /**
     * 获取银行的名称
     * @return 银行的名称
     */
    String getName();
}
