package com.dicomclub.payment.module.pay.model.wxpay;

import com.dicomclub.payment.module.pay.model.PayResponse;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.net.URI;

/**
 * @author ftm
 * @date 2023/2/17 0017 16:39
 */
@Data
@AllArgsConstructor
@Builder
public class WxPayResponse  extends PayResponse {




//    /**
//     * 以下参数只有微信支付会返回 (在微信付款码支付使用)  =============兼容V2的
//     * returnCode returnMsg resultCode errCode errCodeDes
//     */
//    private String returnCode;
//
//    private String returnMsg;
//
//    private String resultCode;
//
//
//
//    private String errCodeDes;
//
//    private String prePayParams;
//
//
//
//    /** 以下字段仅在微信h5支付返回. */
//    private String appId;
//
//    private String timeStamp;
//
//    private String nonceStr;
//
//    @JsonProperty("package")
//    private String packAge;
//
//    private String signType;
//
//    private String paySign;
//
//
//    private String mwebUrl;
//
//
//    private String prepayId;




    @Override
    public ResponseEntity buildNotifyedSuccessResponse() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_XML);
        String body = "<xml><return_code><![CDATA[SUCCESS]]></return_code><return_msg><![CDATA[OK]]></return_msg></xml>";
        return new ResponseEntity<>(body, headers, HttpStatus.OK);
    }
}
