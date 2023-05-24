package com.dicomclub.payment.module.pay.service.wxpay.v3.model;

import com.dicomclub.payment.module.pay.model.common.BillType;
import com.dicomclub.payment.module.pay.service.wxpay.v3.common.WxConst;
import com.dicomclub.payment.module.pay.service.wxpay.v3.common.WxTransactionType;
import org.apache.commons.lang3.StringUtils;

/**
 * @author ftm
 * @date 2023/4/24 0024 11:49
 */
public enum WxAccountType implements BillType {
    /**
     * 基本账户， 不填则默认是数据流
     */
    BASIC("BASIC"),
    /**
     * 基本账户
     * 返回格式为.gzip的压缩包账单
     */
    BASIC_GZIP("BASIC", WxConst.GZIP),
    /**
     * 运营账户
     */
    OPERATION("OPERATION"),
    /**
     * 运营账户
     * 返回格式为.gzip的压缩包账单
     */
    OPERATION_GZIP("OPERATION", WxConst.GZIP),
    /**
     * 手续费账户
     */
    FEES("FEES"),
    /**
     * 手续费账户
     * 返回格式为.gzip的压缩包账单
     */
    FEES_GZIP("FEES", WxConst.GZIP);

    /**
     * 账单类型
     */
    private String type;
    /**
     * 日期格式化表达式
     */
    private String tarType;



    WxAccountType(String type) {
        this.type = type;
    }


    WxAccountType(String type, String tarType) {
        this.type = type;
        this.tarType = tarType;
    }

    /**
     * 获取类型名称
     *
     * @return 类型
     */
    @Override
    public String getType() {
        return type;
    }

    /**
     * 获取类型对应的日期格式化表达式
     *
     * @return 日期格式化表达式
     */
    @Override
    public String getDatePattern() {
        return null;
    }

    /**
     * 获取文件类型
     *
     * @return 文件类型
     */
    @Override
    public String getFileType() {
        return tarType;
    }


    /**
     * 返回交易类型
     *
     * @return 交易类型
     */
    @Override
    public String getCustom() {
        return WxTransactionType.FUND_FLOW_BILL.name();
    }

    public static WxAccountType forType(String type) {
        for (WxAccountType wxPayBillType : WxAccountType.values()) {
            if (wxPayBillType.getType().equals(type) && StringUtils.isEmpty(wxPayBillType.getFileType())) {
                return wxPayBillType;
            }
        }
        return null;
    }

}
