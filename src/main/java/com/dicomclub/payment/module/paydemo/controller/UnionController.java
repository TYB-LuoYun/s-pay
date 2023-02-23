package com.dicomclub.payment.module.paydemo.controller;

import com.dicomclub.payment.module.pay.config.UnionPayConfig;

/**
 * @author ftm
 * @date 2023/2/21 0021 11:17
 */
public class UnionController {
     public static void main(String[] args){
         UnionPayConfig unionPayConfigStorage = new UnionPayConfig();
         //是否为证书签名
         unionPayConfigStorage.setCertSign(true);
         //商户id
         unionPayConfigStorage.setMerId("商户id");
         //公钥，验签证书链格式： 中级证书路径;根证书路径
//        unionPayConfigStorage.setKeyPublic("D:/certs/acp_test_middle.cer;D:/certs/acp_test_root.cer");
         //中级证书路径
         unionPayConfigStorage.setAcpMiddleCert("D:/certs/acp_test_middle.cer");
         //根证书路径
         unionPayConfigStorage.setAcpRootCert("D:/certs/acp_test_root.cer");

         //私钥, 私钥证书格式： 私钥证书路径;私钥证书对应的密码
//        unionPayConfigStorage.setKeyPrivate("D:/certs/acp_test_sign.pfx;000000");
         // 私钥证书路径
         unionPayConfigStorage.setKeyPrivateCert("D:/certs/acp_test_sign.pfx");
         //私钥证书对应的密码
         unionPayConfigStorage.setKeyPrivateCertPwd("000000");

         unionPayConfigStorage.setNotifyUrl("异步回调地址");
         unionPayConfigStorage.setReturnUrl("同步回调地址");
         unionPayConfigStorage.setSignType("RSA2");
         unionPayConfigStorage.setInputCharset("UTF-8");
         //是否为测试账号，沙箱环境
         unionPayConfigStorage.setSandbox(true);



     }
}
