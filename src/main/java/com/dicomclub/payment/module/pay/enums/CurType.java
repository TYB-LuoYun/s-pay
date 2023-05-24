package com.dicomclub.payment.module.pay.enums;

/**
 * @author ftm
 * @date 2023/3/3 0003 11:25
 * 基础货币类型
 */
public interface CurType {
    /**
     * 获取货币类型
     * @return 货币类型
     */
    String getType();

    /**
     * 货币名称
     * @return 货币名称
     */
    String getName();
}
