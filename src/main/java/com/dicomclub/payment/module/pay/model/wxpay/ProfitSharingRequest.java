package com.dicomclub.payment.module.pay.model.wxpay;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author ftm
 * @date 2023/4/15 0015 19:52
 */
@Data
public class ProfitSharingRequest implements Serializable {
    @SerializedName("sub_mchid")
    private String subMchId;
    @SerializedName("appid")
    private String appid;
    @SerializedName("transaction_id")
    private String transactionId;
    @SerializedName("out_order_no")
    private String outOrderNo;
    @SerializedName("receivers")
    private List<ProfitSharingReceiver> receivers;
    @SerializedName("unfreeze_unsplit")
    private boolean unfreezeUnsplit;

    @SerializedName("description")
    private String description;
}
