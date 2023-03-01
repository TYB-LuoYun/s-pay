package com.dicomclub.payment.module.pay.enums;
import lombok.AllArgsConstructor;
import lombok.Getter;

import static com.dicomclub.payment.module.pay.enums.ChannelState.*;

/**
 * @author ftm
 * @date 2023/2/27 0027 11:24
 */
@Getter
@AllArgsConstructor
public enum WxTradeStatusEnum {

    /**
     * "待支付(未支付,订单生成)",
     */
    NOTPAY(WAITING) ,//下单后

    /**
     * "支付中"
     */
    USERPAYING(WAITING) ,

    PAYERROR(CONFIRM_FAIL) ,

    REVOKED(CONFIRM_FAIL) ,

    SUCCESS(CONFIRM_SUCCESS) ,

    REFUND(CONFIRM_SUCCESS) ,//转入退款

    /**
     * <pre>
     * 在指定时间段内   未支付时   关闭的交易；
     * 在交易    完成全额退款成功时  关闭的交易。
     * </pre>
     */
    CLOSED(CONFIRM_FAIL) ,//关闭订单，已不能够重新支付
    ;

    ChannelState channelState;
    public static WxTradeStatusEnum findByName(String name) {
        for (WxTradeStatusEnum statusEnum : WxTradeStatusEnum.values()) {
            if (name.toLowerCase().equals(statusEnum.name().toLowerCase())) {
                return statusEnum;
            }
        }
        throw new RuntimeException("错误的支付宝支付状态");
    }


}
