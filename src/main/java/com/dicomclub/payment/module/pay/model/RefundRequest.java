package com.dicomclub.payment.module.pay.model;

/**
 * @author ftm
 * @date 2023/3/21 0021 13:50
 */

import com.dicomclub.payment.module.pay.enums.CurType;
import com.dicomclub.payment.module.pay.enums.DefaultCurType;
import com.dicomclub.payment.module.pay.enums.PayChannel;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 支付时请求参数
 */
@Data
public class RefundRequest extends AssistOrder{

    /**
     * 支付方式.
     */
    private PayChannel payChannel;


    /**
     * 退款单号()
     * 标识一次退款请求，同一笔交易多次退款需要保证唯一，如需部分退款，则此参数必传。
     */
    private String refundNo;

    /**
     * 订单金额.
     * 微信退款需要
     */
    private BigDecimal orderAmount;

    /**
     * 退款金额
     */
    private BigDecimal refundAmount;


    /**
     * 货币
     */
    private CurType curType = DefaultCurType.CNY;

    /**
     * 退款原因
     */
    private String refundReason;
}

