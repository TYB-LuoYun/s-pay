package com.dicomclub.payment.common.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.codec.binary.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * @author LuoYun
 * @since 2022/6/23 17:18
 */
@Slf4j
public class AESUtil {
    /**
     * "算法/模式/补码方式"
     */
    private static final String AEP = "AES/CBC/PKCS5Padding";


    private static final String DefaultAseIv ="asdef@edsfrIV999";
    private static final String DefaultAseKey = "asdef@edsfrI1399";

    public static String decryptAES(String content ) {
        return decryptAES(content, DefaultAseKey, DefaultAseIv);
    }

    public static String encryptAES(String content ) {
         return encryptAES(content, DefaultAseKey, DefaultAseIv);
    }


    public static String decryptAES(String content,String aseKey ,String aseIv) {
        try {
            Cipher cipher = Cipher.getInstance(AEP);
            IvParameterSpec ivParameterSpec = new IvParameterSpec(aseIv.getBytes("UTF-8"));
            cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(aseKey.getBytes("UTF-8"), "AES"), ivParameterSpec);
            return new String(cipher.doFinal(Base64.decodeBase64(content)));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return null;
        }
    }

    public static String encryptAES(String content,String aseKey ,String aseIv) {
        try {
            IvParameterSpec iv = new IvParameterSpec(aseIv.getBytes("UTF-8"));
            SecretKeySpec skeySpec = new SecretKeySpec(aseKey.getBytes("UTF-8"), "AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);
            byte[] encrypted = cipher.doFinal(content.getBytes());
            return Base64.encodeBase64String(encrypted);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return null;
        }
    }
}
