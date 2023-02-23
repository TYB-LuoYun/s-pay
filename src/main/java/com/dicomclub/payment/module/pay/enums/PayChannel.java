package com.dicomclub.payment.module.pay.enums;


import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 支付渠道
 */
@Getter
@AllArgsConstructor
public enum PayChannel {


    ALIPAY_PC("alipay_pc", PayType.ALIPAY, "支付宝pc"),

    ALIPAY_APP("alipay_app", PayType.ALIPAY, "支付宝app"),

    /**
     * "支付宝WAP支付" 和 "支付宝H5支付" 都是通过浏览器进行支付的方式,实现方式不同,属于手机网站支付(chatapt说的)
     * 不过"支付宝WAP支付"可能会存在一些兼容性问题
     */
    ALIPAY_WAP("alipay_wap", PayType.ALIPAY, "支付宝wap"),

    ALIPAY_H5("alipay_h5", PayType.ALIPAY, "支付宝统一下单(h5)"),

//  扫码付（用户扫码支付）
    ALIPAY_QRCODE("alipay_precreate", PayType.ALIPAY, "支付宝统一收单线下交易预创建"),

//  条码付 (用户付款码支付)
    ALIPAY_BARCODE("alipay_barcode", PayType.ALIPAY, "支付宝统一收单交易支付接口(付款码)"),





    WXPAY_MP("JSAPI", PayType.WX, "微信公众账号支付"),

    WXPAY_MWEB("MWEB", PayType.WX, "微信H5支付"),

    WXPAY_NATIVE("NATIVE", PayType.WX, "微信Native支付"),

    WXPAY_MINI("JSAPI", PayType.WX, "微信小程序支付"),

    WXPAY_APP("APP", PayType.WX, "微信APP支付"),

    WXPAY_MICRO("MICRO", PayType.WX, "微信付款码支付"),



    UNION_WEB_GATEWAY("UNION_PC_GATEWAY", PayType.UNION,"银联PC/平板在线网关支付"),
//  企业网银(B2B)过时了，我就不列了

    UNION_MOBILE_WAP("UNION_MOBILE_WAP", PayType.UNION,"银联手机网页WAP支付"),


    UNION_B2B("UNION_B2B", PayType.UNION,"企业网银支付(过时，建议在线网关支付)"),

    /**
     * 持卡人仅需使用支持银联标准的APP扫
     * https://open.unionpay.com/tjweb/acproduct/list?apiservId=468
     */
    UNION_QRCODE("UNION_QRCODE", PayType.UNION,"申码，扫码付-主扫场景"),

    UNION_CONSUME_BARCODE("UNION_CONSUME_BARCODE", PayType.UNION,"消费(被扫场景)")


//    UNION_MOBILE_CASHIER_DESK("UNION_MOBILE_CASHIER_DESK",UNION,"银联手机线上统一收银台")
//  还有无跳转支付，空间支付，apple支付，暂时不用
    ;

    private String code;

    private PayType payType;

    private String desc;

//    private Class<? extends PayRequest> payRequest;



//    public static PayChannel getByName(String code) {
//        for (PayChannel bestPayTypeEnum : PayChannel.values()) {
//            if (bestPayTypeEnum.name().equalsIgnoreCase(code)) {
//                return bestPayTypeEnum;
//            }
//        }
//        throw new RuntimeException("不支持的渠道");
//    }

    public static PayChannel getByCode(String code) {
        for (PayChannel bestPayTypeEnum : PayChannel.values()) {
            if (bestPayTypeEnum.code.equals(code)) {
                return bestPayTypeEnum;
            }
        }
        throw new RuntimeException("不支持的渠道");
    }
}
