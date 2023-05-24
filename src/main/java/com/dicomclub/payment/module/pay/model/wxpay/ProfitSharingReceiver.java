package com.dicomclub.payment.module.pay.model.wxpay;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

import java.io.Serializable;

/**
 * @author ftm
 * @date 2023/4/15 0015 19:53
 */
@Data
public class ProfitSharingReceiver implements Serializable {
    @SerializedName("sub_mchid")
    private String subMchId;
    @SerializedName("appid")
    private String appid;
    @SerializedName("type")
    private String type;
    @SerializedName("account")
    private String account;
    @SerializedName("name")
//    @SpecEncrypt
    private String name;
    @SerializedName("relation_type")
    private String relationType;
    @SerializedName("custom_relation")
    private String customRelation;
    private String description;
    private Long amount;
}
