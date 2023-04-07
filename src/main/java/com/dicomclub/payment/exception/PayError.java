package com.dicomclub.payment.exception;

/**
 * @author ftm
 * @date 2023/3/3 0003 13:21
 */
public interface PayError {

    /**
     * 获取错误码
     *
     * @return 错误码
     */
    String getErrorCode();

    /**
     * 获取错误消息
     *
     * @return 错误消息
     */
    String getErrorMsg();

    /**
     * 获取异常信息
     * @return 异常信息
     */
    String getString();

}
