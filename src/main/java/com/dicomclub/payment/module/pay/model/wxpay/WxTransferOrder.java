package com.dicomclub.payment.module.pay.model.wxpay;

import com.dicomclub.payment.module.pay.model.TransferOrder;
import com.dicomclub.payment.module.pay.model.wxpay.request.TransferBatchesRequest;
import com.dicomclub.payment.module.pay.model.wxpay.request.TransferDetail;
import lombok.Data;

import java.util.List;

/**
 * @author ftm
 * @date 2023/3/27 0027 14:07
 */
@Data
public class WxTransferOrder extends TransferOrder {
    /**
     * 转账总笔数,一个转账批次单最多发起三千笔转账。转账总笔数必须与批次内所有明细之和保持一致，否则无法发起转账操作
     */
    private Integer totalNum = 1;

    /**
     * 必填，指定该笔转账使用的转账场景ID
     */
    private String transferSceneId;


    /**
     * 发起批量转账的明细列表，最多三千笔
     */
    private List<TransferDetail> transferDetailList;



}
