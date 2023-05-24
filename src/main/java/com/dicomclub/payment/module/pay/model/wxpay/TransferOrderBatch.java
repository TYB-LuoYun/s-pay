package com.dicomclub.payment.module.pay.model.wxpay;

import com.dicomclub.payment.module.pay.model.TransferOrder;
import com.dicomclub.payment.module.pay.model.wxpay.request.WxTransferDetail;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author ftm
 * @date 2023/4/28 0028 16:37
 */
@Data
public class TransferOrderBatch {
    /**
     * 转账批次号
     */
    private String transferBatchNo;


    private String batchName;

    private String remark;

    /**
     * 转账总金额
     */
    private BigDecimal totalAmount;


    /**
     * 转账总笔数
     */
    private Integer totalNum ;





    List<TransferOrder> transferDetails;
}
