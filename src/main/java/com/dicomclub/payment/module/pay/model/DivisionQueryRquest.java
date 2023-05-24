package com.dicomclub.payment.module.pay.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author ftm
 * @date 2023/4/18 0018 10:40
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DivisionQueryRquest {
    /**
     * 订单号.
     */
    private String divisionBatchNo;


    /**
     * 第三方支付的流水号
     */
    private String outTradeNo;
}
