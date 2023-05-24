package com.dicomclub.payment.module.pay.common;

import org.apache.commons.lang3.StringUtils;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.*;

/**
 * @author ftm
 * @date 2023/3/21 0021 11:06
 */
public class AesUtils {
    static final int KEY_LENGTH_BYTE = 32;
    static final int TAG_LENGTH_BIT = 128;
    private final byte[] aesKey;

    public AesUtils(byte[] key) {
        if (key.length != 32) {
            throw new IllegalArgumentException("无效的ApiV3Key，长度必须为32个字节");
        } else {
            this.aesKey = key;
        }
    }

    public static byte[] decryptToByte(byte[] nonce, byte[] cipherData, byte[] key) throws GeneralSecurityException {
        return decryptToByte((byte[])null, nonce, cipherData, key);
    }

    public static byte[] decryptToByte(byte[] associatedData, byte[] nonce, byte[] cipherData, byte[] key) throws GeneralSecurityException {
        try {
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            SecretKeySpec secretKeySpec = new SecretKeySpec(key, "AES");
            GCMParameterSpec spec = new GCMParameterSpec(128, nonce);
            cipher.init(2, secretKeySpec, spec);
            if (associatedData != null) {
                cipher.updateAAD(associatedData);
            }

            return cipher.doFinal(cipherData);
        } catch (NoSuchPaddingException | NoSuchAlgorithmException var7) {
            throw new IllegalStateException(var7);
        } catch (InvalidAlgorithmParameterException | InvalidKeyException var8) {
            throw new IllegalArgumentException(var8);
        }
    }

    public String decryptToString(byte[] associatedData, byte[] nonce, String ciphertext) throws GeneralSecurityException, IOException {
        try {
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            SecretKeySpec key = new SecretKeySpec(this.aesKey, "AES");
            GCMParameterSpec spec = new GCMParameterSpec(128, nonce);
            cipher.init(2, key, spec);
            cipher.updateAAD(associatedData);
            return new String(cipher.doFinal(Base64.getDecoder().decode(StringUtils.remove(ciphertext, " "))), StandardCharsets.UTF_8);
        } catch (NoSuchPaddingException | NoSuchAlgorithmException var7) {
            throw new IllegalStateException(var7);
        } catch (InvalidAlgorithmParameterException | InvalidKeyException var8) {
            throw new IllegalArgumentException(var8);
        }
    }

    public static String decryptToString(String associatedData, String nonce, String ciphertext, String apiV3Key) throws GeneralSecurityException, IOException {
        try {
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            SecretKeySpec key = new SecretKeySpec(apiV3Key.getBytes(), "AES");
            GCMParameterSpec spec = new GCMParameterSpec(128, nonce.getBytes());
            cipher.init(2, key, spec);
            cipher.updateAAD(associatedData.getBytes());
            return new String(cipher.doFinal(Base64.getDecoder().decode(ciphertext)), StandardCharsets.UTF_8);
        } catch (NoSuchPaddingException | NoSuchAlgorithmException var7) {
            throw new IllegalStateException(var7);
        } catch (InvalidAlgorithmParameterException | InvalidKeyException var8) {
            throw new IllegalArgumentException(var8);
        }
    }

    public static String createSign(Map<String, String> map, String mchKey) {
        Map<String, String> params = map;
        SortedMap<String, String> sortedMap = new TreeMap(map);
        StringBuilder toSign = new StringBuilder();
        Iterator var5 = sortedMap.keySet().iterator();

        while(var5.hasNext()) {
            String key = (String)var5.next();
            String value = (String)params.get(key);
            if (!"sign".equals(key) && !StringUtils.isEmpty(value)) {
                toSign.append(key).append("=").append(value).append("&");
            }
        }

        toSign.append("key=" + mchKey);
        return HMACSHA256(toSign.toString(), mchKey);
    }

    public static String HMACSHA256(String data, String key) {
        try {
            Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
            SecretKeySpec secret_key = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            sha256_HMAC.init(secret_key);
            byte[] array = sha256_HMAC.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            byte[] var6 = array;
            int var7 = array.length;

            for(int var8 = 0; var8 < var7; ++var8) {
                byte item = var6[var8];
                sb.append(Integer.toHexString(item & 255 | 256).substring(1, 3));
            }

            return sb.toString().toUpperCase();
        } catch (Exception var10) {
            var10.printStackTrace();
            return null;
        }
    }
}
