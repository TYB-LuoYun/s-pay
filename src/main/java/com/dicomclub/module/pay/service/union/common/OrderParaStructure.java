package com.dicomclub.module.pay.service.union.common;

import org.apache.commons.lang3.StringUtils;

import java.util.Date;
import java.util.Map;

/**
 * @author ftm
 * @date 2023/2/21 0021 11:54
 * 订单参数构造器
 */
public final class OrderParaStructure {
    private OrderParaStructure() {
    }

    public static Map<String, Object> loadParameters(Map<String, Object> parameters, String key, String value) {
        if (StringUtils.isNotEmpty(value)) {
            parameters.put(key, value);
        }
        return parameters;
    }

//    public static Map<String, Object> loadParameters(Map<String, Object> parameters, String key, Order order) {
//        Object attr = order.getAttr(key);
//        if (null != attr && !"".equals(attr)) {
//            order.getAttrs().remove(key);
//            parameters.put(key, attr);
//        }
//        return parameters;
//    }

//    public static Map<String, Object> loadDateParameters(Map<String, Object> parameters, String key, Order order, String datePattern) {
//        return OrderParaStructure.loadParameters(parameters, key, DateUtils.formatDate((Date) order.getAttr(key), datePattern));
//    }


}
