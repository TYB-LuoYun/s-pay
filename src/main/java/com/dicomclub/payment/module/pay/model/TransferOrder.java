package com.dicomclub.payment.module.pay.model;

import com.dicomclub.payment.module.pay.enums.CurType;
import com.dicomclub.payment.module.pay.model.common.Bank;
import com.dicomclub.payment.module.pay.model.common.CountryCode;
import com.dicomclub.payment.module.pay.model.wxpay.request.TransferBatchesRequest;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * @author ftm
 * @date 2023/3/27 0027 11:33
 */
@Data
public class TransferOrder {
    /**
     * 转账订单号
     */
    private String transferId;


    /**
     * 转账金额
     */
    private BigDecimal amount;


    /**
     * 收款人姓名
     */
    private String accountName;

    /**
     * 收款账号
     */
    private String accountNo;

    /**
     * 收款人开户行名称
     */
    private String bankName;

    /**
     * 转账备注信息
     */
    private String transferDesc;





}
