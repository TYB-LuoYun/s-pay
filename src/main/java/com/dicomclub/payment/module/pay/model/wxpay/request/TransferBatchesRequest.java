package com.dicomclub.payment.module.pay.model.wxpay.request;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author ftm
 * @date 2023/3/27 0027 11:45
 */
@Data
public class TransferBatchesRequest implements Serializable {
    @SerializedName("appid")
    private String appid;
    @SerializedName("out_batch_no")
    private String outBatchNo;
    @SerializedName("batch_name")
    private String batchName;
    @SerializedName("batch_remark")
    private String batchRemark;
    @SerializedName("total_amount")
    private Integer totalAmount;
    @SerializedName("total_num")
    private Integer totalNum;
//    @SpecEncrypt
    @SerializedName("transfer_detail_list")
    private List<WxTransferDetail> transferDetailList;



}


