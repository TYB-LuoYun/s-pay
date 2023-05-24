package com.dicomclub.payment.module.pay.model;

import com.dicomclub.payment.module.pay.common.ChannelStateRes;
import com.dicomclub.payment.module.pay.enums.ChannelState;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author ftm
 * @date 2023/3/27 0027 11:42
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class TransferResponse {
    private ChannelStateRes channelStateRes;
}
