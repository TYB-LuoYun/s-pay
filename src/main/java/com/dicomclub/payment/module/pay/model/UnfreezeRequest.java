package com.dicomclub.payment.module.pay.model;

import lombok.Data;

/**
 * @author ftm
 * @date 2023/7/7 0007 9:44
 */
@Data
public class UnfreezeRequest {
    /**
     * 订单号.
     */
    private String orderNo;


    /**
     * 第三方支付的流水号
     */
    private String outTradeNo;

    /**
     * 系统分账批次号 --- 微信需要，微信把解冻当分账处理的
     */
    private String divisionBatchNo;
}
