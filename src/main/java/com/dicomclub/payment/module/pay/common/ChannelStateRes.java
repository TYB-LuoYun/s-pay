package com.dicomclub.payment.module.pay.common;

import com.dicomclub.payment.module.pay.enums.ChannelState;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author ftm
 * @date 2023/3/21 0021 16:28
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class ChannelStateRes {
    private String code;
//  消息
    private String msg;
//  数据包
    private Object data;
    private ChannelState channelState;
}
