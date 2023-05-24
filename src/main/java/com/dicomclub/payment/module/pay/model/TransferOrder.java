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
     String transferNo;
     BigDecimal amount;
     String remark;
     String accountNo;
     String accountName;
}
