package com.dicomclub.module.pay.model;

import com.dicomclub.module.pay.enums.PayType;
import lombok.Data;

/**
 * @author ftm
 * @date 2023/2/15 0015 11:39
 */
@Data
public abstract class PayResponse {

    /**
     * 支付返回的body体，html 可直接嵌入网页使用
     */
    private String body;




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


    /**
     * 扫码付模式二用来生成二维码
     */
    private String codeUrl;


    private PayType payType;

}
