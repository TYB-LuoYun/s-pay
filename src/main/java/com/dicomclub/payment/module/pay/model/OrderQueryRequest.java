package com.dicomclub.payment.module.pay.model;

import com.dicomclub.payment.module.pay.enums.PayType;
import lombok.Data;

/**
 * @author ftm
 * @date 2023/2/27 0027 9:37
 */
@Data
public class OrderQueryRequest {
    /**
     * 支付平台.
     */
    private PayType payType;

    /**
     * 订单号(orderId 和 outOrderId 二选一，两个都传以outOrderId为准)
     */
    private String orderNo = "";

    /**
     * 外部订单号(例如微信生成的)
     */
    private String outOrderNo = "";
}
