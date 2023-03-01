package com.dicomclub.payment.module.pay.model;

import com.dicomclub.payment.module.pay.enums.ChannelState;
import com.dicomclub.payment.module.pay.enums.PayDataType;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.ResponseEntity;

/**
 * @author ftm
 * @date 2023/2/15 0015 11:39
 */
@Data
public abstract class PayResponse {



    private ChannelState channelState;




    /**
     * 订单ID
     */
    private String orderId;

    /**
     * 第三方支付的流水号
     */
    private String outTradeNo;

    /**
     * 订单金额
     */
    private Double orderAmount;


    /**
     * 备注，附加
     */
    private String attach;



    private String errCode;


    private String errMsg;



//  结果
    /**
     * 支付返回的body体，html 可直接嵌入网页使用
     */
    private String body;

    /** 跳转地址 **/
    private String payUrl;

    /** 二维码地址 **/
    private String codeUrl;

    /** 二维码图片地址 **/
    private String codeImgUrl;

    /** 表单内容 **/
    private String formContent;



    public String buildPayDataType(){
        if(StringUtils.isNotEmpty(payUrl)){
        return PayDataType.PAY_URL.getCode();
    }

        if(StringUtils.isNotEmpty(codeUrl)){
        return PayDataType.CODE_URL.getCode();
    }

        if(StringUtils.isNotEmpty(codeImgUrl)){
        return PayDataType.CODE_IMG_URL.getCode();
    }

     if(StringUtils.isNotEmpty(formContent)){
        return PayDataType.FORM.getCode();
    }

        if(StringUtils.isNotEmpty(body)){
            return PayDataType.HTML.getCode();
        }

        return PayDataType.PAY_URL.getCode();
    }

    public String buildPayData(){
        if(StringUtils.isNotEmpty(payUrl)){
            return payUrl;
        }

        if(StringUtils.isNotEmpty(codeUrl)){
            return codeUrl;
        }

        if(StringUtils.isNotEmpty(codeImgUrl)){
            return codeImgUrl;
        }

        if(StringUtils.isNotEmpty(formContent)){
            return formContent;
        }

        if(StringUtils.isNotEmpty(body)){
            return body;
        }

        return "";
    }


    abstract public ResponseEntity buildPaySuccessResponse();
}
