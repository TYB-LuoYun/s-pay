package com.dicomclub.payment.module.pay.service.wxpay.v3.model;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

import java.io.Serializable;

/**
 * @author ftm
 * @date 2023/3/28 0028 14:03
 */
@Data
public class WxPayRefundNotifyV3Result implements Serializable {
    private OriginNotifyResponse rawData;
    private WxPayRefundNotifyV3Result.DecryptNotifyResult result;


    @Data
    public static class DecryptNotifyResult implements Serializable {
        private static final long serialVersionUID = -1L;
        @SerializedName("mchid")
        private String mchid;
        @SerializedName("out_trade_no")
        private String outTradeNo;
        @SerializedName("transaction_id")
        private String transactionId;
        @SerializedName("out_refund_no")
        private String outRefundNo;
        @SerializedName("refund_id")
        private String refundId;
        @SerializedName("refund_status")
        private String refundStatus;
        @SerializedName("success_time")
        private String successTime;
        @SerializedName("user_received_account")
        private String userReceivedAccount;
        @SerializedName("amount")
        private WxPayRefundNotifyV3Result.Amount amount;



    }
    @Data
    public static class Amount implements Serializable {
        private static final long serialVersionUID = 1L;
        @SerializedName("total")
        private Integer total;
        @SerializedName("refund")
        private Integer refund;
        @SerializedName("payer_total")
        private Integer payerTotal;
        @SerializedName("payer_refund")
        private Integer payerRefund;
    }
}
