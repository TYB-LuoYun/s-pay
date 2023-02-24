package com.dicomclub.payment.module.pay.service.alipay.tool;

import com.alipay.api.*;
import com.dicomclub.payment.exception.PayException;
import com.dicomclub.payment.module.pay.config.AliPayConfig;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * @author ftm
 * @date 2023/2/24 0024 11:51
 */
@Slf4j
@Data
@AllArgsConstructor
public class AlipayClientWrapper {

    //默认为 不使用证书方式 , 0
    private Boolean useCert = false;

    /**
     * 缓存支付宝client 对象
     **/
    private AlipayClient alipayClient;

    /**
     * 封装支付宝接口调用函数
     **/
    public <T extends AlipayResponse> T execute(AlipayRequest<T> request) {

        try {

            T alipayResp = null;

            if (useCert != null && useCert == true) { //证书加密方式
                alipayResp = alipayClient.certificateExecute(request);

            } else { //key 或者 空都为默认普通加密方式
                alipayResp = alipayClient.execute(request);
            }

            return alipayResp;

        } catch (AlipayApiException e) { // 调起接口前出现异常，如私钥问题。  调起后出现验签异常等。

            log.error("调起支付宝execute[AlipayApiException]异常！", e);
            //如果数据返回出现验签异常，则需要抛出： UNKNOWN 异常。
            throw new PayException(e.getMessage());

        } catch (Exception e) {
            log.error("调起支付宝execute[Exception]异常！", e);
            throw new PayException("调用支付宝client服务异常");
        }
    }


    /*
     * 构建支付宝client 包装类
     *
     * @author terrfly
     * @site https://www.jeequan.com
     * @date 2021/6/8 17:46
     */
    public static AlipayClientWrapper buildAlipayClientWrapper(Boolean useCert, Boolean sandbox, String appId, String privateKey, String alipayPublicKey, String signType, String appCert,
                                                               String alipayPublicCert, String alipayRootCert) {

        //避免空值
        sandbox = sandbox == null ? false : sandbox;

        AlipayClient alipayClient = null;
        if (useCert != null && useCert == true) { //证书的方式
            throw new PayException("证书方式暂未实现");
//            ChannelCertConfigKitBean channelCertConfigKitBean = SpringBeansUtil.getBean(ChannelCertConfigKitBean.class);
//
//            CertAlipayRequest certAlipayRequest = new CertAlipayRequest();
//            certAlipayRequest.setServerUrl(sandbox == true ? AliPayConfig.SANDBOX_SERVER_URL : AliPayConfig.PROD_SERVER_URL);
//            certAlipayRequest.setAppId(appId);
//            certAlipayRequest.setPrivateKey(privateKey);
//            certAlipayRequest.setFormat(AliPayConfig.FORMAT);
//            certAlipayRequest.setCharset(AliPayConfig.CHARSET);
//            certAlipayRequest.setSignType(signType);
//
//            certAlipayRequest.setCertPath(channelCertConfigKitBean.getCertFilePath(appCert));
//            certAlipayRequest.setAlipayPublicCertPath(channelCertConfigKitBean.getCertFilePath(alipayPublicCert));
//            certAlipayRequest.setRootCertPath(channelCertConfigKitBean.getCertFilePath(alipayRootCert));
//            try {
//                alipayClient = new DefaultAlipayClient(certAlipayRequest);
//            } catch (AlipayApiException e) {
//                log.error("error" ,e);
//                alipayClient = null;
//            }
        } else {
            alipayClient = new DefaultAlipayClient(sandbox == true ? AliPayConfig.SANDBOX_SERVER_URL : AliPayConfig.PROD_SERVER_URL
                    , appId, privateKey, AliPayConfig.FORMAT, AliPayConfig.CHARSET,
                    alipayPublicKey, signType);
        }

        return new AlipayClientWrapper(useCert, alipayClient);
    }


    public static AlipayClientWrapper buildAlipayClientWrapper(AliPayConfig alipayParams) {

        return buildAlipayClientWrapper(
                alipayParams.getUseCert(), alipayParams.isSandbox(), alipayParams.getAppId(), alipayParams.getPrivateKey(),
                alipayParams.getAliPayPublicKey(), alipayParams.getSignType(), alipayParams.getAppPublicCert(),
                alipayParams.getAliPayPublicCert(), alipayParams.getAliPayRootCert()
        );
    }

}




