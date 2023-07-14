package com.dicomclub.payment.module.pay.service.nanjing.model;

import lombok.Data;

/**
 * @author ftm
 * @date 2023/6/30 0030 17:02
 */
@Data
public class NJReq {
    private String appID;

    private ReqHead reqHead;

    private String reqData;

    private String key;

    @Data
    public static class ReqHead{
        private String  channelID ;
        private String serviceID;
        private String seqNo;
        private String transDate;
        private String transTime;
        private String  signData ;
        private String encryptFlag;
    }
}
