package com.dicomclub.payment.module.pay.model.wxpay;

import com.dicomclub.payment.module.pay.enums.WxReceiverType;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @author ftm
 * @date 2023/4/15 0015 20:46
 */
@Data
public class DivisionReceiver {

    /**
     * 分账接收账号类型
     */
    private String accountType;

    /**
     * 分账接收账号
     */
    private String accountNo;

    /**
     * 接收方的分账金额
     */
    private BigDecimal divisionAmount;




}
