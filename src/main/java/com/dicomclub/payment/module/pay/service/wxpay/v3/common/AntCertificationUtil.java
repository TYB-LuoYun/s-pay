package com.dicomclub.payment.module.pay.service.wxpay.v3.common;


import com.dicomclub.payment.exception.PayError;
import com.dicomclub.payment.exception.PayErrorException;
import com.dicomclub.payment.exception.PayException;
import com.dicomclub.payment.exception.WxPayError;
import com.dicomclub.payment.module.pay.service.union.common.sign.SignUtils;
import com.dicomclub.payment.module.pay.service.union.common.sign.encrypt.Base64;
import com.dicomclub.payment.module.pay.service.wxpay.config.CertEnvironment;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.cert.*;
import java.security.cert.Certificate;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.management.openmbean.InvalidKeyException;

/**
 * @author ftm
 * @date 2023/3/3 0003 13:18
 * 证书文件可信校验
 */
@Slf4j
public class AntCertificationUtil {
    /**
     * 微信平台证书容器  key = 序列号  value = 证书对象
     */
    private static final Map<String, Certificate> CERTIFICATE_MAP = new ConcurrentHashMap<>();

    private AntCertificationUtil() {
    }

    private static final KeyStore PKCS12_KEY_STORE;

    private static final CertificateFactory CERTIFICATE_FACTORY;

    static {
        String javaVersion = System.getProperty("java.version");
        if (javaVersion.contains("1.8") || javaVersion.startsWith("8")){
            Security.setProperty("crypto.policy", "unlimited");
        }
        SignUtils.initBc();
        try {
            PKCS12_KEY_STORE = KeyStore.getInstance("PKCS12");
        }
        catch (KeyStoreException e) {
            throw new PayErrorException(new WxPayError(WxConst.FAILURE, " keystore 初始化失败"), e);
        }

        try {
            CERTIFICATE_FACTORY = CertificateFactory.getInstance("X509", WxConst.BC_PROVIDER);
        }
        catch (NoSuchProviderException | CertificateException e) {
            throw new PayErrorException(new WxPayError(WxConst.FAILURE, " keystore 初始化失败"), e);
        }

    }



    /**
     * 获取公钥证书序列号
     *
     * @param certContent 公钥证书内容
     * @return 公钥证书序列号
     */
    public static String getCertSN(String certContent) {
        try {
            InputStream inputStream = new ByteArrayInputStream(certContent.getBytes());
            CertificateFactory factory = CertificateFactory.getInstance("X.509", "BC");
            X509Certificate cert = (X509Certificate) factory.generateCertificate(inputStream);
            return md5((cert.getIssuerX500Principal().getName() + cert.getSerialNumber()).getBytes());
        }
        catch (GeneralSecurityException e) {
            throw new PayException(" 获取公钥证书序列号异常"+ e.getMessage());
        }
    }

    private static String md5(byte[] bytes) {
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("MD5");
        }
        catch (NoSuchAlgorithmException e) {
            throw new PayException(""+e.getMessage());
        }
        md.update(bytes);
        String certSN = new BigInteger(1, md.digest()).toString(16);
        //BigInteger会把0省略掉，需补全至32位
        certSN = fillMD5(certSN);
        return certSN;
    }


    /**
     * 装载平台证书
     *
     * @param serialNo          证书序列
     * @param certificateStream 证书流
     * @return 平台证书
     */
    public static Certificate loadCertificate(String serialNo, InputStream certificateStream) {
        try {
            Certificate certificate = CERTIFICATE_FACTORY.generateCertificate(certificateStream);
            CERTIFICATE_MAP.put(serialNo, certificate);
            return certificate;
        }
        catch (CertificateException e) {
            throw new PayErrorException(new WxPayError(WxConst.FAILURE, " 在生成微信v3证书时发生错误，原因是" + e.getMessage()), e);
        }

    }

    /**
     * 获取平台证书
     *
     * @param serialNo 证书序列
     * @return 平台证书
     */
    public static Certificate getCertificate(String serialNo) {
        return CERTIFICATE_MAP.get(serialNo);

    }

    /**
     * 获取公私钥.
     *
     * @param keyCertStream 商户API证书
     * @param keyAlias      证书别名
     * @param keyPass       证书对应的密码
     * @return 证书信息集合
     */
    public static CertEnvironment initCertification(InputStream keyCertStream, String keyAlias, String keyPass) {

        char[] pem = keyPass.toCharArray();
        try {
            PKCS12_KEY_STORE.load(keyCertStream, pem);
            X509Certificate certificate = (X509Certificate) PKCS12_KEY_STORE.getCertificate(keyAlias);
            certificate.checkValidity();
            String serialNumber = certificate.getSerialNumber().toString(16).toUpperCase();
            PublicKey publicKey = certificate.getPublicKey();
            PrivateKey privateKey = (PrivateKey) PKCS12_KEY_STORE.getKey(keyAlias, pem);
            return new CertEnvironment(privateKey, publicKey, serialNumber,certificate);
        }
        catch (InvalidKeyException e) {
            throw new PayErrorException(new WxPayError(WxConst.FAILURE, "获取公私钥失败， 解决方式：替换jre包：local_policy.jar，US_export_policy.jar"), e);
        }
        catch (GeneralSecurityException e) {
            throw new PayErrorException(new WxPayError(WxConst.FAILURE, "获取公私钥失败"), e);
        }
        catch (IOException e) {
            throw new PayErrorException(new WxPayError(WxConst.FAILURE, "私钥证书流加载失败"), e);
        }

    }


    /**
     * 解密响应体.
     *
     * @param associatedData    相关数据
     * @param nonce             随机串
     * @param cipherText        需要解密的文本
     * @param secretKey         密钥
     * @param characterEncoding 编码类型
     * @return 解密后的信息
     */
    public static String decryptToString(String associatedData, String nonce, String cipherText, String secretKey, String characterEncoding) {

        try {
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding", WxConst.BC_PROVIDER);
            SecretKeySpec key = new SecretKeySpec(secretKey.getBytes(Charset.forName(characterEncoding)), "AES");
            GCMParameterSpec spec = new GCMParameterSpec(128, nonce.getBytes(Charset.forName(characterEncoding)));
            cipher.init(Cipher.DECRYPT_MODE, key, spec);
            cipher.updateAAD(associatedData.getBytes(Charset.forName(characterEncoding)));
            byte[] bytes = cipher.doFinal(Base64.decode(cipherText));
            return new String(bytes, Charset.forName(characterEncoding));
        }
        catch (GeneralSecurityException e) {
            throw new PayErrorException(new WxPayError(WxConst.FAILURE, e.getMessage()), e);
        }
    }

    /**
     * 对请求敏感字段进行加密
     *
     * @param message     the message
     * @param certificate the certificate
     * @return 加密后的内容
     */
    public static String encryptToString(String message, Certificate certificate) {
        try {
            Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-1AndMGF1Padding", WxConst.BC_PROVIDER);
            cipher.init(Cipher.ENCRYPT_MODE, certificate.getPublicKey());

            byte[] data = message.getBytes(StandardCharsets.UTF_8);
            byte[] cipherData = cipher.doFinal(data);
            return Base64.encode(cipherData);

        }
        catch (GeneralSecurityException e) {
            throw new PayErrorException(new WxPayError(WxConst.FAILURE, e.getMessage()), e);
        }
    }



    public static X509Certificate getCertificate(InputStream inputStream) {
        try {
            CertificateFactory cf = CertificateFactory.getInstance("X509");
            X509Certificate cert = (X509Certificate)cf.generateCertificate(inputStream);
            cert.checkValidity();
            return cert;
        } catch (CertificateExpiredException var3) {
            throw new RuntimeException("证书已过期", var3);
        } catch (CertificateNotYetValidException var4) {
            throw new RuntimeException("证书尚未生效", var4);
        } catch (CertificateException var5) {
            throw new RuntimeException("无效的证书", var5);
        }
    }


    public static String readFromInputStream(InputStream cert) {
        try {
            return new String(IOUtils.toByteArray(cert), StandardCharsets.UTF_8);
        }
        catch (IOException e) {
            throw new PayException("证书加载异常！"+e.getMessage());
        }
    }


    /**
     * 获取支付宝根证书序列号
     *
     * @param rootCertContent 支付宝根证书内容
     * @return 支付宝根证书序列号
     */
    public static String getRootCertSN(String rootCertContent) {
        String rootCertSN = null;
        try {
            X509Certificate[] x509Certificates = readPemCertChain(rootCertContent);
            if (null == x509Certificates) {
                return null;
            }
            MessageDigest md = MessageDigest.getInstance("MD5");
            for (X509Certificate c : x509Certificates) {
                if (c.getSigAlgOID().startsWith("1.2.840.113549.1.1")) {
                    md.update((c.getIssuerX500Principal().getName() + c.getSerialNumber()).getBytes());
                    String certSN = new BigInteger(1, md.digest()).toString(16);
                    //BigInteger会把0省略掉，需补全至32位
                    certSN = fillMD5(certSN);
                    if (StringUtils.isEmpty(rootCertSN)) {
                        rootCertSN = certSN;
                    }
                    else {
                        rootCertSN = rootCertSN + "_" + certSN;
                    }
                }

            }
        }
        catch (NoSuchAlgorithmException e) {
            log.error("提取根证书失败", e);
        }
        return rootCertSN;
    }

    private static String fillMD5(String md5) {
        return md5.length() == 32 ? md5 : fillMD5("0" + md5);
    }



    private static X509Certificate[] readPemCertChain(String cert) {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(cert.getBytes());
        try {
            CertificateFactory factory = CertificateFactory.getInstance("X.509", "BC");
            ;
            Collection<? extends Certificate> certificates = factory.generateCertificates(inputStream);
            return certificates.toArray(new X509Certificate[certificates.size()]);
        }
        catch (GeneralSecurityException e) {
            log.error("提取根证书失败", e);
        }
        return null;

    }


    /**
     * 提取公钥证书中的公钥
     *
     * @param certContent 公钥证书内容
     * @return 公钥证书中的公钥
     */
    public static String getCertPublicKey(String certContent) {
        try {
            InputStream inputStream = new ByteArrayInputStream(certContent.getBytes());
            CertificateFactory factory = CertificateFactory.getInstance("X.509", "BC");
            X509Certificate cert = (X509Certificate) factory.generateCertificate(inputStream);
            return Base64.encode(cert.getPublicKey().getEncoded());
        }
        catch (GeneralSecurityException e) {
            throw new PayException(" 提取公钥证书中的公钥异常", e.getMessage());
        }
    }


}
