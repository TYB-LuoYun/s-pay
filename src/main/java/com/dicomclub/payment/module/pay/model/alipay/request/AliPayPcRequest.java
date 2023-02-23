package com.dicomclub.payment.module.pay.model.alipay.request;

import lombok.Data;

/**
 * Created by this
 * @date 2019/9/8 15:19
 */
@Data
public class AliPayPcRequest {

    /**
     * app_id
     */
    private String appId;
    /**
     * 接口名称
     */
    private String method;
    /**
     * 请求使用的编码格式，如utf-8,gbk,gb2312等
     */
    private String charset;
    /**
     * 生成签名字符串所使用的签名算法类型，目前支持RSA2和RSA，推荐使用RSA2
     */
    private String signType;
    /**
     * 商户请求参数的签名串，详见签名 https://docs.open.alipay.com/291/105974
     */
    private String sign;
    /**
     * 发送请求的时间，格式"yyyy-MM-dd HH:mm:ss"
     */
    private String timestamp;
    /**
     * 调用的接口版本，固定为：1.0
     */
    private String version;
    /**
     * 支付宝服务器主动通知商户服务器里指定的页面http/https路径。
     */
    private String notifyUrl;
    /**
     * 请求参数的集合，最大长度不限，除公共参数外所有请求参数都必须放在这个参数中传递，具体参照各产品快速接入文档
     */
    private String bizContent;
    /**
     * HTTP/HTTPS开头字符串
     */
    private String returnUrl;
}
