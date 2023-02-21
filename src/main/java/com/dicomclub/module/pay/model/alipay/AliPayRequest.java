package com.dicomclub.module.pay.model.alipay;

import com.dicomclub.module.pay.model.PayRequest;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author ftm
 * @date 2023/2/15 0015 11:44
 */
@Data
public class AliPayRequest extends PayRequest {
    /**
     * 买家支付宝账号
     */
    private String buyerLogonId;

    /**
     * 买家的支付宝唯一用户号（2088开头的16位纯数字）
     */
    private String buyerId;

    /**
     * 支付后跳转（支付宝PC网站支付）
     * 优先级高于PayConfig.returnUrl
     */
    private String returnUrl;


}
