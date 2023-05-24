package com.dicomclub.payment.module.pay.model;

import com.dicomclub.payment.module.pay.model.wxpay.DivisionReceiver;
import lombok.Data;

import java.util.List;

/**
 * @author ftm
 * @date 2023/4/15 0015 19:47
 */
@Data
public class DivisionRquest  {
    /**
     * 订单号.
     */
    private String orderNo;


    /**
     * 第三方支付的流水号
     */
    private String outTradeNo;

    /**
     * 系统分账批次号
     */
    private String divisionBatchNo;



    private List<DivisionReceiver> receivers;

}
