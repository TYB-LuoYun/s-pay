package com.dicomclub.payment.module.pay.model.wxpay;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author ftm
 * @date 2023/4/15 0015 21:12
 */
@Data
public class ProfitSharingResult {
    @SerializedName("sub_mchid")
    private String subMchId;
    @SerializedName("transaction_id")
    private String transactionId;
    @SerializedName("out_order_no")
    private String outOrderNo;
    @SerializedName("order_id")
    private String orderId;
    @SerializedName("state")
    private String state;
    @SerializedName("receivers")
    private List<ProfitSharingResult.Receiver> receivers;

    @Data
    public static class Receiver implements Serializable {
        @SerializedName("type")
        private String type;
        @SerializedName("account")
        private String account;
        @SerializedName("amount")
        private Long amount;
        @SerializedName("description")
        private String description;
        @SerializedName("result")
        private String result;
        @SerializedName("fail_reason")
        private String failReason;
        @SerializedName("create_time")
        private String createTime;
        @SerializedName("finish_time")
        private String finishTime;
        @SerializedName("detail_id")
        private String detailId;
    }
}
