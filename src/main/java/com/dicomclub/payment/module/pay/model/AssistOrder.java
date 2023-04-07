package com.dicomclub.payment.module.pay.model;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * @author ftm
 * @date 2023/3/21 0021 14:13
 * 协助订单-退款，支付等通用
 */
@Data
public class AssistOrder {
    /**
     * 订单号.
     */
    private String orderNo;


    /**
     * 第三方支付的流水号
     */
    private String outTradeNo;


    /**
     * 自定义订单通知地址
     */
    private String notifyUrl;

    /**
     * 订单附加信息，可用于预设未提供的参数，这里会覆盖以上所有的订单信息，
     */
    @JSONField(serialize = false)
    private volatile Map<String, Object> attr;


    public Map<String, Object> getAttrs() {
        if (null == attr) {
            attr = new HashMap<>();
        }
        return attr;
    }

    public Object getAttr(String key) {
        return getAttrs().get(key);
    }


    /**
     * 添加订单信息
     *
     * @param key   key
     * @param value 值
     */
    public void addAttr(String key, Object value) {
        getAttrs().put(key, value);
    }

}
