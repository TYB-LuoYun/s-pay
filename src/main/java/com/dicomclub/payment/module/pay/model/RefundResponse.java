package com.dicomclub.payment.module.pay.model;

import com.alibaba.fastjson.JSONObject;
import com.dicomclub.payment.module.pay.common.ChannelStateRes;
import com.dicomclub.payment.module.pay.enums.ChannelState;
import com.dicomclub.payment.module.pay.enums.PayType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

/**
 * @author ftm
 * @date 2023/3/21 0021 13:49
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public  class RefundResponse {

    private ChannelStateRes channelStateRes;

    private PayType payType;


    private String refundNo;
    /**
     * 订单号.
     */
    private String orderNo;

    /**
     * 订单金额.
     */
    private Double refundAmount;



    /**
     * 第三方退款流水号.
     */
    private String outRefundNo;


    private String finishTime;



    public ResponseEntity buildNotifyedSuccessResponse() {
        if(PayType.WX == payType){
            JSONObject resJSON = new JSONObject();
            resJSON.put("code", "SUCCESS");
            resJSON.put("message", "成功");
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.setContentType(MediaType.APPLICATION_JSON);
            return new ResponseEntity( resJSON, httpHeaders, HttpStatus.OK);
        }
        return null;
    }
}

