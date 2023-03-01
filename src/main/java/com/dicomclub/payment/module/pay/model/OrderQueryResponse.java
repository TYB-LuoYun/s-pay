package com.dicomclub.payment.module.pay.model;

import com.dicomclub.payment.module.pay.enums.ChannelState;
import lombok.Builder;
import lombok.Data;

/**
 * @author ftm
 * @date 2023/2/27 0027 10:54
 */
@Data
@Builder
public class OrderQueryResponse {

    private ChannelState channelState;

    /**
     *第三方支付的流水号
     */
    private String outTradeNo;


    /**
     * 订单号
     */
    private String orderNo;

    /**
     * 附加内容，发起支付时传入
     */
    private String attach;

    /**
     * 错误原因
     */
    private String resultMsg;



    /**
     * 支付完成时间
     */
    private String finishTime;
}
