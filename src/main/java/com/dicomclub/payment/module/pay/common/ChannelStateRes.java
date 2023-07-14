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

    public static ChannelStateRes confirmSuccess(Object data) {
        return ChannelStateRes.builder().channelState(ChannelState.CONFIRM_SUCCESS).data(data).build();
    }


    public static ChannelStateRes waiting() {
        return ChannelStateRes.builder().channelState(ChannelState.WAITING).build();
    }


    public static ChannelStateRes fail(String msg) {
        return ChannelStateRes.builder().channelState(ChannelState.CONFIRM_FAIL).data(null).msg(msg).build();
    }
}
