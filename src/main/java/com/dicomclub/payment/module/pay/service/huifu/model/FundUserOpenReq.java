package com.dicomclub.payment.module.pay.service.huifu.model;

import lombok.Data;

import java.util.Map;

/**
 * @author ftm
 * @date 2023-08-07 15:06
 */
@Data
public class FundUserOpenReq {
    /**
     * 申请流水号
     */
    private String applyNo;

    private Integer fundUserType; //用户类型-0-个人，1-企业


    private String name; //个人或企业名

    private String shortName; //简称,个人不填


    /**
     *  身份信息（个人或者法人信息）
     */
    private IdentityInfo identityInfo;


    /**
     *  营业许可信息
     */
    private BusinessLicenseInfo businessLicenseInfo;


    /**
     *  结算卡信息
     */
    private CardInfo  cardInfo;


    /**
     *  联系人信息
     */
    private ContactInfo contactInfo;


    /**
     *  扩展信息
     */
    private Map<String,Object> extParams;


    @Data
    public static class CardInfo{
        private Integer cardType; //cardType ,0：对公账户 1：对私法人账户 2：对私非法人账户；示例值：0  个人商户/用户不支持填写对公账户和对私非法人账户。
        private String cardName; //结算银行卡对应的户名；  当card_type=0时填写企业名称 ; 当card_type=1时填写法人姓名，对私法人结算银行户名与法人姓名必需一致； 当card_type=2时填写非法人姓名
        private String cardNo; //结算银行卡号
        private String provinceCode; //	银行所在省
        private String areaCode; //	银行所在市
        private String bankCode; //银行编号 ,当card_type=0时必填， 当card_type=1或2时非必填
        private String branchCode; //银行支行编码 当card_type=0时必填， 当card_type=1或2时非必填
        private String branchName; //支行名称,https://pay.weixin.qq.com/wiki/doc/apiv3_partner/apis/chapter11_1_1.shtml
    }


    @Data
    public static class ContactInfo{
        private String contactName;
        private String contactPhone;
        private String contactEmail;
    }




    @Data
    public static class IdentityInfo{
        private String certType;//身份证00
        private String certNo;//个人证件号码
        private Integer certValidityType = 0; //1长期有效
        private String certBeginDate;
        private String certEndDate;
    }


    @Data
    public static class BusinessLicenseInfo{
        private String licenseCode;
        private Integer licenseValidityType;
        private String licenseBeginDate;
        private String licenseEndDate;
        private String regProvinceCode;
        private String regAreaCode;
        private String regDistrictCode;
        private String regAddressDetail;
    }


}
