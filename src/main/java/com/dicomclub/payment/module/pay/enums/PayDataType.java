package com.dicomclub.payment.module.pay.enums;

import com.dicomclub.payment.exception.PayException;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author ftm
 * @date 2023/2/23 0023 9:16
 * 支付请求返回数据类型
 */
@Getter
@AllArgsConstructor
public enum PayDataType {
    PAY_URL("payUrl","跳转链接的方式  redirectUrl"),
    FORM("form","表单提交"),
    HTML("html","生成的页面"),
    CODE_URL("codeUrl","二维码URL"),
    CODE_IMG_URL("codeImgUrl","二维码图片显示URL"),
    DATA_MAP("dataMap","数据对"),



    WX_APP("wxApp","微信app参数"),
    ALI_APP("aliApp","支付宝app参数"),
    YSF_APP("ysfApp","云闪付app参数"),
    NONE("none","无参数");

    private String code;

    private String name;


    public static PayDataType getByCode(String code) {
        for (PayDataType bestPayTypeEnum : PayDataType.values()) {
            if (bestPayTypeEnum.code.equals(code)) {
                return bestPayTypeEnum;
            }
        }
        return null;
    }
}
