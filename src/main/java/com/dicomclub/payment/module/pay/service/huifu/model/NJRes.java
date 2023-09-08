package com.dicomclub.payment.module.pay.service.huifu.model;

import lombok.Data;

/**
 * @author ftm
 * @date 2023/6/30 0030 17:03
 */
@Data
public class NJRes {
    private RspHead rspHead;
    private String rspData;

    @Data
    public static class RspHead{
        private String serviceID;
        private String seqNo;
        private String transDate;
        private String transTime;
        private String returnCode;
        private String signData ;
        private String returnMsg;
    }
}
