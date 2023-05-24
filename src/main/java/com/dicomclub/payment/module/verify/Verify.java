package com.dicomclub.payment.module.verify;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author ftm
 * @date 2023/1/31 0031 17:30
 * RSA 加密是为了防止信息被泄露，而签名是为了防止信息被篡改
 * RSA 加密一般用公钥加密，签名 一般用 私钥加密
 * RSA 效率比 AES 低，AES是非对称加密（加解密使用同一个钥匙）,RSA 非对称的，AES试用于加密长数据
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE,ElementType.METHOD})
public @interface Verify {
   String value() default VerifyType.MD5withRSA;
}
