package com.dicomclub.payment.module.pay.model.wxpay.request;

import com.alibaba.fastjson.annotation.JSONField;

/**
 * @author ftm
 * @date 2023/3/27 0027 14:10
 */
public class TransferDetail {
    /**
     * 商家明细单号
     */
    @JSONField(name = "out_detail_no")
    private String outDetailNo;
    /**
     * 转账金额，单位为分
     */
    @JSONField(name = "transfer_amount")
    private Integer transferAmount;
    /**
     * 单条转账备注（微信用户会收到该备注），UTF8编码，最多允许32个字符
     */
    @JSONField(name = "transfer_remark")
    private String transferRemark;
    /**
     * 用户在直连商户appid下的唯一标识
     */
    private String openid;
    /**
     * 收款用户姓名
     */
    @JSONField(name = "user_name")
    private String userName;
    /**
     * 收款用户身份证
     */
    @JSONField(name = "user_id_card")
    private String userIdCard;


    public TransferDetail() {
    }

    public TransferDetail(String outDetailNo, Integer transferAmount, String transferRemark, String openid) {
        this.outDetailNo = outDetailNo;
        this.transferAmount = transferAmount;
        this.transferRemark = transferRemark;
        this.openid = openid;
    }

    public String getOutDetailNo() {
        return outDetailNo;
    }

    public void setOutDetailNo(String outDetailNo) {
        this.outDetailNo = outDetailNo;
    }

    public Integer getTransferAmount() {
        return transferAmount;
    }

    public void setTransferAmount(Integer transferAmount) {
        this.transferAmount = transferAmount;
    }

    public String getTransferRemark() {
        return transferRemark;
    }

    public void setTransferRemark(String transferRemark) {
        this.transferRemark = transferRemark;
    }

    public String getOpenid() {
        return openid;
    }

    public void setOpenid(String openid) {
        this.openid = openid;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserIdCard() {
        return userIdCard;
    }

    public void setUserIdCard(String userIdCard) {
        this.userIdCard = userIdCard;
    }
}
