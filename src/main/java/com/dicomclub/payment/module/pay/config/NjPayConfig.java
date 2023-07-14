package com.dicomclub.payment.module.pay.config;

import lombok.Data;

/**
 * @author ftm
 * @date 2023/6/30 0030 17:54
 */
@Data
public class NjPayConfig extends PayConfig{
    private String appId;



    /**
     * 商户编号
     */
    private String merchantId;


}
