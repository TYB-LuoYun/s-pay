package com.dicomclub.payment.module.pay.model;

import com.dicomclub.payment.module.pay.common.ChannelStateRes;
import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * @author ftm
 * @date 2023/4/15 0015 19:43
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DivisionResponse {
    private ChannelStateRes channelStateRes;
    /**
     * 系统分账批次号
     */
    private String divisionBatchNo;


    /**
     * 系统分账批次号
     */
    private String outDivisionBatchNo;


    private List<Receiver> receivers;



    @Data
    public static class Receiver implements Serializable {
        private ChannelStateRes channelStateRes;


        private String finishTime;


        private String account;


        private BigDecimal amount;

        /**
         * 微信分账明细单号，每笔分账业务执行的明细单号，可与资金账单对账使用
         */
        private String divisionDetailNo;
    }
}
