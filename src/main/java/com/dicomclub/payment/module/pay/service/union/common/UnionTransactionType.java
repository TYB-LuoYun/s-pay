package com.dicomclub.payment.module.pay.service.union.common;

import com.dicomclub.payment.module.pay.constants.UnionPayConstants;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

/**
 * @author ftm
 * @date 2023/2/21 0021 12:38
 */
public enum UnionTransactionType  {

    /**
     * ---尚未接入---
     * 苹果支付
     * 官方文档：https://open.unionpay.com/tjweb/acproduct/list?apiservId=460
     */
    APPLE_PAY("01","01","000802","08"),
    /**
     * 手机控件
     */
    APP("01","01","000000","08"),
    /**
     *  08 -指的是手机
     */
    WAP("01","01","000201","08"),
    /**
     * 网关支付(B2C) 07 -  PC,平板
     * 官方文档：https://open.unionpay.com/tjweb/acproduct/list?apiservId=453
     */
    WEB("01","01","000201","07"),
    /**
     * ---尚未接入---
     * 无跳转支付()
     * 官方文档：https://open.unionpay.com/tjweb/acproduct/list?apiservId=449
     */
    NO_JUMP("01","01","000301","07"),
    /**
     * 企业网银支付（B2B支付）
     * 官方文档：https://open.unionpay.com/tjweb/acproduct/list?apiservId=452
     */
    B2B("01","01","000202","07"),
    /**
     *  申码(主扫场景)
     *  官方文档：https://open.unionpay.com/tjweb/acproduct/list?apiservId=468
     */
    APPLY_QR_CODE("01","07","000000","08"),
    /**
     * 消费(被扫场景)
     * 官方文档:同申码(主扫场景)
     */
    CONSUME("01","06","000000","08"),
    //消费撤销
    CONSUME_UNDO("31","00","000000","08"),
    //退款-https://open.unionpay.com/tjweb/acproduct/APIList?acpAPIId=756&apiservId=448&version=V2.2&bussType=0
    REFUND("04","00","000000","08"),
    //查询
    QUERY("00","00","000201",""),
    //对账文件下载
    FILE_TRANSFER("76","01","000000","")
    ;

    /**
     * 交易类型
     */
    private String txnType;
    /**
     * 交易子类型
     */
    private String txnSubType;

    /**
     * 业务类型
     */
    private String bizType;

    /**
     * 渠道类型 05：语音 07：PC,平板  08：手机 16：数字机顶盒
     */
    private String channelType;


    UnionTransactionType (String txnType, String txnSubType, String bizType, String channelType) {
        this.txnType = txnType;
        this.txnSubType = txnSubType;
        this.bizType = bizType;
        this.channelType = channelType;
    }

    public void convertMap(Map<String ,Object> contentData){
        //交易类型
        contentData.put(UnionPayConstants.param_txnType, this.getTxnType());
        //交易子类
        contentData.put(UnionPayConstants.param_txnSubType,this.getTxnSubType());
        //业务类型
        contentData.put(UnionPayConstants.param_bizType,this.getBizType());
        //渠道类型
        if(StringUtils.isNotBlank(this.getChannelType())){
            contentData.put(UnionPayConstants.param_channelType,this.getChannelType());
        }
    }


    public String getTxnType () {
        return txnType;
    }

    public String getTxnSubType () {
        return txnSubType;
    }

    public String getBizType () {
        return bizType;
    }

    public String getChannelType () {
        return channelType;
    }

    /**
     *获取交易对类型枚举
     *
     * @return 交易类型
     */
    public String getType () {
        return this.name();
    }

}

