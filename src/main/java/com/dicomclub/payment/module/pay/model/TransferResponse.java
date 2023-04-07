package com.dicomclub.payment.module.pay.model;

import com.dicomclub.payment.module.pay.enums.ChannelState;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

/**
 * @author ftm
 * @date 2023/3/27 0027 11:42
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TransferResponse {
    private ChannelState channelState;

    /**
     * 转账订单号
     */
    private String transferId;
}
