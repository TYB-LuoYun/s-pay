package com.dicomclub.payment.module.pay.model.wxpay.response;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

import java.io.Serializable;

/**
 * @author ftm
 * @date 2023/3/27 0027 14:22
 */
@Data
public class TransferBatchesResult implements Serializable {
    @SerializedName("out_batch_no")
    private String outBatchNo;
    @SerializedName("batch_id")
    private String batchId;
    @SerializedName("create_time")
    private String createTime;
}
