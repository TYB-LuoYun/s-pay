package com.dicomclub.module.pay.model.wxpay;

import com.dicomclub.module.pay.model.PayResponse;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.net.URI;

/**
 * @author ftm
 * @date 2023/2/17 0017 16:39
 */
@Data
public class WxPayResponse  extends PayResponse {
    /**
     * 以下参数只有微信支付会返回 (在微信付款码支付使用)
     * returnCode returnMsg resultCode errCode errCodeDes
     */
    private String returnCode;

    private String returnMsg;

    private String resultCode;

    private String errCode;

    private String errCodeDes;

    private String prePayParams;

    private URI payUri;

    /** 以下字段仅在微信h5支付返回. */
    private String appId;

    private String timeStamp;

    private String nonceStr;

    @JsonProperty("package")
    private String packAge;

    private String signType;

    private String paySign;


    private String mwebUrl;


    private String prepayId;








}
