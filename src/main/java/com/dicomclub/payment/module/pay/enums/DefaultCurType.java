package com.dicomclub.payment.module.pay.enums;

/**
 * @author ftm
 * @date 2023/3/3 0003 11:25
 */
public enum DefaultCurType implements CurType{

    CNY("人民币"),
    USD("美元"),
    HKD("港币"),
    MOP("澳门元"),
    EUR("欧元"),
    TWD("新台币"),
    KRW("韩元"),
    JPY("日元"),
    SGD("新加坡元"),
    AUD("澳大利亚元");
    /**
     * 币种名称
     */
    private String name;

    /**
     * 构造函数
     * @param name
     */
    DefaultCurType(String name) {
        this.name = name;
    }

    /**
     * 获取货币类型
     *
     * @return 货币类型
     */
    @Override
    public String getType() {
        return this.name();
    }
    /**
     * 货币名称
     *
     * @return 货币名称
     */
    @Override
    public String getName() {
        return name;
    }
}
