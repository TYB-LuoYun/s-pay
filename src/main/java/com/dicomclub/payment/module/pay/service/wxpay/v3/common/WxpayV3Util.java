package com.dicomclub.payment.module.pay.service.wxpay.v3.common;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.dicomclub.payment.common.utils.HttpClientUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

/**
 * @author ftm
 * @date 2023/3/21 0021 15:54
 */
@Slf4j
public class WxpayV3Util {

    private static final String PAY_BASE_URL = "https://api.mch.weixin.qq.com";
    public static final Map<String, String> NORMALMCH_URL_MAP = new HashMap<>();
    static {
        NORMALMCH_URL_MAP.put(WxPayConstants.TradeType.APP, "/v3/pay/transactions/app");
        NORMALMCH_URL_MAP.put(WxPayConstants.TradeType.JSAPI, "/v3/pay/transactions/jsapi");
        NORMALMCH_URL_MAP.put(WxPayConstants.TradeType.NATIVE, "/v3/pay/transactions/native");
        NORMALMCH_URL_MAP.put(WxPayConstants.TradeType.MWEB, "/v3/pay/transactions/h5");
    }

    public static final Map<String, String> ISV_URL_MAP = new HashMap<>();
    static {
        ISV_URL_MAP.put(WxPayConstants.TradeType.APP, "/v3/pay/partner/transactions/app");
        ISV_URL_MAP.put(WxPayConstants.TradeType.JSAPI, "/v3/pay/partner/transactions/jsapi");
        ISV_URL_MAP.put(WxPayConstants.TradeType.NATIVE, "/v3/pay/partner/transactions/native");
        ISV_URL_MAP.put(WxPayConstants.TradeType.MWEB, "/v3/pay/partner/transactions/h5");
    }

//    public static JSONObject unifiedOrderV3(String reqUrl, JSONObject reqJSON)  {
//        String response = postV3(PAY_BASE_URL + reqUrl, reqJSON.toJSONString());
//        return JSONObject.parseObject(getPayInfo(response, getConfig()));
//    }

    private static String postV3(String url, String toJSONString) {
       return HttpClientUtil.doPost(url, toJSONString, "UTF-8");
    }
    private static String getV3(String url) {
        return HttpClientUtil.doGet(url,null , "UTF-8");
    }
    public static JSONObject queryOrderV3(String url)  {
        String response = getV3(PAY_BASE_URL + url);
        return JSON.parseObject(response);
    }



    public static JSONObject closeOrderV3(String url, JSONObject reqJSON)  {
        String response = postV3(PAY_BASE_URL + url, reqJSON.toJSONString());
        return JSON.parseObject(response);
    }

    public static JSONObject refundV3(JSONObject reqJSON)  {
        String url = String.format("%s/v3/refund/domestic/refunds", PAY_BASE_URL);
        String response = postV3(url, reqJSON.toJSONString());
        return JSON.parseObject(response);
    }

    public static JSONObject refundQueryV3(String refundOrderId)  {
        String url = String.format("%s/v3/refund/domestic/refunds/%s", PAY_BASE_URL, refundOrderId);
        String response = getV3(url);
        return JSON.parseObject(response);
    }

    public static JSONObject refundQueryV3Isv(String refundOrderId,String SubMchId)  {
        String url = String.format("%s/v3/refund/domestic/refunds/%s?sub_mchid=%s", PAY_BASE_URL, refundOrderId, SubMchId);
        String response = getV3(url);
        return JSON.parseObject(response);
    }


//    public static String getPayInfo(String response, WxPayConfig wxPayConfig)   {
//
//        try {
//            JSONObject resJSON = JSON.parseObject(response);
//            String timestamp = String.valueOf(System.currentTimeMillis() / 1000L);
//            String nonceStr = SignUtils.genRandomStr();
//            String prepayId = resJSON.getString("prepay_id");
//
//            switch(wxPayConfig.getTradeType()) {
//                case WxPayConstants.TradeType.JSAPI: {
//                    Map<String, String> payInfo = new HashMap<>(); // 如果用JsonObject会出现签名错误
//
//                    String appid = wxPayConfig.getAppId(); // 用户在服务商appid下的唯一标识
//                    if (StringUtils.isNotEmpty(wxPayConfig.getSubAppId())) {
//                        appid = wxPayConfig.getSubAppId(); // 用户在子商户appid下的唯一标识
//                    }
//
//                    payInfo.put("appId", appid);
//                    payInfo.put("timeStamp", timestamp);
//                    payInfo.put("nonceStr", nonceStr);
//                    payInfo.put("package", "prepay_id=" + prepayId);
//                    payInfo.put("signType", "RSA");
//
//                    String beforeSign = String.format("%s\n%s\n%s\n%s\n", appid, timestamp, nonceStr, "prepay_id=" + prepayId);
//                    payInfo.put("paySign", SignUtils.sign(beforeSign, PemUtils.loadPrivateKey(new FileInputStream(wxPayConfig.getPrivateKeyPath()))));
//                    // 签名以后在增加prepayId参数
//                    payInfo.put("prepayId", prepayId);
//                    return JSON.toJSONString(payInfo);
//                }
//                case WxPayConstants.TradeType.MWEB: {
//                    return response;
//                }
//                case WxPayConstants.TradeType.APP: {
//                    Map<String, String> payInfo = new HashMap<>();
//                    // APP支付绑定的是微信开放平台上的账号，APPID为开放平台上绑定APP后发放的参数
//                    String wxAppId = wxPayConfig.getAppId();
//                    // 此map用于参与调起sdk支付的二次签名,格式全小写，timestamp只能是10位,格式固定，切勿修改
//                    String partnerId = wxPayConfig.getMchId();
//
//                    if (StringUtils.isNotEmpty(wxPayConfig.getSubAppId())) {
//                        wxAppId = wxPayConfig.getSubAppId();
//                        partnerId = wxPayConfig.getSubMchId();
//                    }
//
//                    String packageValue = "Sign=WXPay";
//                    // 此map用于客户端与微信服务器交互
//                    String beforeSign = String.format("%s\n%s\n%s\n%s\n", wxAppId, timestamp, nonceStr, prepayId);
//                    payInfo.put("sign", SignUtils.sign(beforeSign, PemUtils.loadPrivateKey(new FileInputStream(wxPayConfig.getPrivateKeyPath()))));
//                    payInfo.put("prepayId", prepayId);
//                    payInfo.put("partnerId", partnerId);
//                    payInfo.put("appId", wxAppId);
//                    payInfo.put("package", packageValue);
//                    payInfo.put("timeStamp", timestamp);
//                    payInfo.put("nonceStr", nonceStr);
//                    return JSON.toJSONString(payInfo);
//                }
//                case WxPayConstants.TradeType.NATIVE:
//                    return response;
//                default:
//                    return null;
//            }
//        } catch (Exception e) {
//            throw (e instanceof PayException) ? (PayException) e : new PayException(e.getMessage(), e);
//        }
//    }

    public static JSONObject processIsvPayer(String subAppId, String openId) {
        JSONObject payer = new JSONObject();
        // 子商户subAppId不为空
        if (StringUtils.isNotBlank(subAppId)) {
            payer.put("sub_openid", openId); // 用户在子商户appid下的唯一标识
        }else {
            payer.put("sp_openid", openId); // 用户在服务商appid下的唯一标识
        }
        return payer;
    }

}
