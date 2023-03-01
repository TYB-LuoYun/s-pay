package com.dicomclub.payment.module.pay.enums;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.omg.CORBA.UNKNOWN;

/**
 * @author ftm
 * @date 2023/2/28 0028 9:08
 */
@AllArgsConstructor
public enum ChannelState {
    CONFIRM_SUCCESS, //接口正确返回： 业务状态已经明确成功
    CONFIRM_FAIL, //接口正确返回： 业务状态已经明确失败
    WAITING, //接口正确返回： 上游处理中， 需通过定时查询/回调进行下一步处理
    UNKNOWN, //接口超时，或网络异常等请求， 或者返回结果的签名失败： 状态不明确 ( 上游接口变更, 暂时无法确定状态值 )
    API_RET_ERROR, //渠道侧出现异常( 接口返回了异常状态 )
    SYS_ERROR //本系统出现不可预知的异常
}
