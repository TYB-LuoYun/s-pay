package com.dicomclub.payment.module.pay.model.union;

import com.alibaba.fastjson.annotation.JSONField;
import com.dicomclub.payment.module.pay.model.PayRequest;
import lombok.Data;

import java.util.Date;
import java.util.Map;

/**
 * @author ftm
 * @date 2023/2/21 0021 11:44
 */
@Data
public class UnionPayRequest extends PayRequest {
    private String notifyUrl;

    /**
     * 订单过期时间
     */
    private Date expirationTime;


    /**
     * 订单附加信息，可用于预设未提供的参数，这里会覆盖以上所有的订单信息，
     */
    @JSONField(serialize = false)
    private volatile Map<String, Object> attrs;


}
