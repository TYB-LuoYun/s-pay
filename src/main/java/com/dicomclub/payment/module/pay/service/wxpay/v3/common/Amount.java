package com.dicomclub.payment.module.pay.service.wxpay.v3.common;

import com.dicomclub.payment.module.pay.enums.CurType;
import com.dicomclub.payment.module.pay.enums.DefaultCurType;
import com.dicomclub.payment.module.pay.service.union.common.Util;

import java.math.BigDecimal;

/**
 * @author ftm
 * @date 2023/3/3 0003 11:24
 */
public class Amount {
    /**
     * 订单总金额，单位为分。
     */
    private Integer total;
    /**
     * 货币类型 CNY：人民币，境内商户号仅支持人民币。
     */
    private String currency = DefaultCurType.CNY.getType();

    public Amount() {
    }

    public Amount(Integer total) {
        this.total = total;
    }

    public Amount(Integer total, String currency) {
        this.total = total;
        this.currency = currency;
    }

    public Integer getTotal() {
        return total;
    }

    public void setTotal(Integer total) {
        this.total = total;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }
    /**
     * 订单金额信息
     *
     * @param order 支付订单
     * @return 订单金额信息
     */
    /**
     *  订单金额信息
     * @param total 金额，这里单位为元
     * @param curType 货币类型
     * @return 订单金额信息
     */
    public static Amount getAmount(BigDecimal total, CurType curType ) {
        // 总金额单位为分
        Amount amount = new Amount(Util.conversionCentAmount(total));
        if (null == curType) {
            curType = DefaultCurType.CNY;
        }
        amount.setCurrency(curType.getType());
        return amount;
    }
}
