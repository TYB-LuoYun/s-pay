package com.dicomclub.payment.module.pay.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author ftm
 * @date 2023/3/29 0029 14:21
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RefundQueryRequest {

    /**
     * 内部
     */
    private String refundNo = "";


    /**
     * 内部订单号
     */
    private String orderNo = "";

    /**
     * 外部订单号(例如微信生成的)
     */
    private String outOrderNo = "";
}
