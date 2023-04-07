package com.dicomclub.payment.module.pay.service.wxpay.v3.common;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author ftm
 * @date 2023/3/3 0003 12:33
 */
public class MapGen<K, V> {

    /**
     * 属性
     */
    private Map<K, V> attr;

    public MapGen(K key, V value) {
        keyValue(key, value);
    }

    public MapGen<K, V> keyValue(K key, V value) {
        if (null == attr){
            attr = new LinkedHashMap<>();
        }
        attr.put(key, value);
        return this;
    }


    public Map<K, V> getAttr() {
        return attr;
    }

    private void setAttr(Map<K, V> attr) {
        this.attr = attr;
    }
}
