package com.dicomclub.payment.module.pay.service.huifu.model;

import com.dicomclub.payment.module.pay.common.ChannelStateRes;
import lombok.Data;

import java.util.Map;

/**
 * @author ftm
 * @date 2023-08-07 14:21
 */
@Data
public class FundUserOpenRes {
    private ChannelStateRes channelStateRes;
    private String fundUserNo;
    private String fundUserType;
    /**
     * 其他参数
     */
    private volatile Map<String, Object> attr;
}
