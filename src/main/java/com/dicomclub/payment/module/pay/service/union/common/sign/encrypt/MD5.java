package com.dicomclub.payment.module.pay.service.union.common.sign.encrypt;


import com.dicomclub.payment.module.pay.service.union.common.str.StringUtils;
import org.apache.commons.codec.digest.DigestUtils;

/**
 * MD5签名工具
 * @author egan
 * <pre>
 * email egzosn@gmail.com
 *</pre>
 */
public class MD5 {

    /**
     * 签名字符串
     *
     * @param text          需要签名的字符串
     * @param key           密钥
     * @param inputCharset 编码格式
     * @return 签名结果
     */
    public static String sign(String text, String key, String inputCharset) {
        //拼接key
        text = text + key;
        return DigestUtils.md5Hex(StringUtils.getContentBytes(text, inputCharset));
    }

    /**
     * 签名字符串
     *
     * @param text          需要签名的字符串
     * @param sign          签名结果
     * @param key           密钥
     * @param inputCharset 编码格式
     * @return 签名结果
     */
    public static boolean verify(String text, String sign, String key, String inputCharset) {
        //判断是否一样
        return StringUtils.equals(sign(text, key, inputCharset).toUpperCase(), sign.toUpperCase());
    }




}