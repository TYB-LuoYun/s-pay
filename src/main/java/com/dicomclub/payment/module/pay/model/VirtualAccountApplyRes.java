package com.dicomclub.payment.module.pay.model;

import com.dicomclub.payment.module.pay.common.ChannelStateRes;
import com.dicomclub.payment.module.pay.enums.PayType;
import lombok.Data;

/**
 * @author ftm
 * @date 2023/7/12 0012 11:10
 */
@Data
public class VirtualAccountApplyRes {
    private ChannelStateRes channelStateRes;

    private PayType payType;
    private String virAccountType;
    private String virAccountNo;
    private String virAccountName;

}
