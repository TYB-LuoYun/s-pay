package com.dicomclub.payment.module.pay.model.wxpay;

import com.dicomclub.payment.module.pay.enums.WxReceiverType;
import com.dicomclub.payment.module.pay.enums.RelationType;
import lombok.Data;

/**
 * @author ftm
 * @date 2023/4/18 0018 10:58
 */
@Data
public class DivisionReceiverBind  {
    /**
     * 分账接收账号类型: 0-个人 1-商户
     */
    private String accountType;

    /**
     * 分账接收账号
     */
    private String accountNo;


    private String accountName;

    private RelationType relationType;

    private String customRelation;

}
