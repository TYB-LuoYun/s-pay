package com.dicomclub.payment.module.pay.common;

/**
 * @author ftm
 * @date 2023/3/3 0003 13:02
 */
public interface TransactionType {
    /**
     * 获取交易类型
     * @return 交易类型
     */
    String getType();

    /**
     * 获取接口
     * @return 接口
     */
    String getMethod();
}
