package com.dicomclub.payment.module.pay.service.wxpay.v3.model;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author ftm
 * @date 2023/3/21 0021 10:52
 */

public class WxPayOrderNotifyV3Result implements Serializable {
    private static final long serialVersionUID = -1L;
    private OriginNotifyResponse rawData;
    private WxPayOrderNotifyV3Result.DecryptNotifyResult result;

    public OriginNotifyResponse getRawData() {
        return this.rawData;
    }

    public WxPayOrderNotifyV3Result.DecryptNotifyResult getResult() {
        return this.result;
    }

    public void setRawData(OriginNotifyResponse rawData) {
        this.rawData = rawData;
    }

    public void setResult(WxPayOrderNotifyV3Result.DecryptNotifyResult result) {
        this.result = result;
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } else if (!(o instanceof WxPayOrderNotifyV3Result)) {
            return false;
        } else {
            WxPayOrderNotifyV3Result other = (WxPayOrderNotifyV3Result)o;
            if (!other.canEqual(this)) {
                return false;
            } else {
                Object this$rawData = this.getRawData();
                Object other$rawData = other.getRawData();
                if (this$rawData == null) {
                    if (other$rawData != null) {
                        return false;
                    }
                } else if (!this$rawData.equals(other$rawData)) {
                    return false;
                }

                Object this$result = this.getResult();
                Object other$result = other.getResult();
                if (this$result == null) {
                    if (other$result != null) {
                        return false;
                    }
                } else if (!this$result.equals(other$result)) {
                    return false;
                }

                return true;
            }
        }
    }

    protected boolean canEqual(Object other) {
        return other instanceof WxPayOrderNotifyV3Result;
    }


    public String toString() {
        return "WxPayOrderNotifyV3Result(rawData=" + this.getRawData() + ", result=" + this.getResult() + ")";
    }

    public WxPayOrderNotifyV3Result() {
    }

    public static class GoodsDetail implements Serializable {
        private static final long serialVersionUID = 1L;
        @SerializedName("goods_id")
        private String goodsId;
        @SerializedName("quantity")
        private Integer quantity;
        @SerializedName("unit_price")
        private Integer unitPrice;
        @SerializedName("discount_amount")
        private Integer discountAmount;
        @SerializedName("goods_remark")
        private String goodsRemark;

        public String getGoodsId() {
            return this.goodsId;
        }

        public Integer getQuantity() {
            return this.quantity;
        }

        public Integer getUnitPrice() {
            return this.unitPrice;
        }

        public Integer getDiscountAmount() {
            return this.discountAmount;
        }

        public String getGoodsRemark() {
            return this.goodsRemark;
        }

        public void setGoodsId(String goodsId) {
            this.goodsId = goodsId;
        }

        public void setQuantity(Integer quantity) {
            this.quantity = quantity;
        }

        public void setUnitPrice(Integer unitPrice) {
            this.unitPrice = unitPrice;
        }

        public void setDiscountAmount(Integer discountAmount) {
            this.discountAmount = discountAmount;
        }

        public void setGoodsRemark(String goodsRemark) {
            this.goodsRemark = goodsRemark;
        }

        public boolean equals(Object o) {
            if (o == this) {
                return true;
            } else if (!(o instanceof WxPayOrderNotifyV3Result.GoodsDetail)) {
                return false;
            } else {
                WxPayOrderNotifyV3Result.GoodsDetail other = (WxPayOrderNotifyV3Result.GoodsDetail)o;
                if (!other.canEqual(this)) {
                    return false;
                } else {
                    label71: {
                        Object this$quantity = this.getQuantity();
                        Object other$quantity = other.getQuantity();
                        if (this$quantity == null) {
                            if (other$quantity == null) {
                                break label71;
                            }
                        } else if (this$quantity.equals(other$quantity)) {
                            break label71;
                        }

                        return false;
                    }

                    Object this$unitPrice = this.getUnitPrice();
                    Object other$unitPrice = other.getUnitPrice();
                    if (this$unitPrice == null) {
                        if (other$unitPrice != null) {
                            return false;
                        }
                    } else if (!this$unitPrice.equals(other$unitPrice)) {
                        return false;
                    }

                    label57: {
                        Object this$discountAmount = this.getDiscountAmount();
                        Object other$discountAmount = other.getDiscountAmount();
                        if (this$discountAmount == null) {
                            if (other$discountAmount == null) {
                                break label57;
                            }
                        } else if (this$discountAmount.equals(other$discountAmount)) {
                            break label57;
                        }

                        return false;
                    }

                    Object this$goodsId = this.getGoodsId();
                    Object other$goodsId = other.getGoodsId();
                    if (this$goodsId == null) {
                        if (other$goodsId != null) {
                            return false;
                        }
                    } else if (!this$goodsId.equals(other$goodsId)) {
                        return false;
                    }

                    Object this$goodsRemark = this.getGoodsRemark();
                    Object other$goodsRemark = other.getGoodsRemark();
                    if (this$goodsRemark == null) {
                        if (other$goodsRemark == null) {
                            return true;
                        }
                    } else if (this$goodsRemark.equals(other$goodsRemark)) {
                        return true;
                    }

                    return false;
                }
            }
        }

        protected boolean canEqual(Object other) {
            return other instanceof WxPayOrderNotifyV3Result.GoodsDetail;
        }


        public String toString() {
            return "WxPayOrderNotifyV3Result.GoodsDetail(goodsId=" + this.getGoodsId() + ", quantity=" + this.getQuantity() + ", unitPrice=" + this.getUnitPrice() + ", discountAmount=" + this.getDiscountAmount() + ", goodsRemark=" + this.getGoodsRemark() + ")";
        }

        public GoodsDetail() {
        }
    }

    public static class PromotionDetail implements Serializable {
        @SerializedName("coupon_id")
        private String couponId;
        @SerializedName("name")
        private String name;
        @SerializedName("scope")
        private String scope;
        @SerializedName("type")
        private String type;
        @SerializedName("amount")
        private Integer amount;
        @SerializedName("stock_id")
        private String stockId;
        @SerializedName("wechatpay_contribute")
        private Integer wechatpayContribute;
        @SerializedName("merchant_contribute")
        private Integer merchantContribute;
        @SerializedName("other_contribute")
        private Integer otherContribute;
        @SerializedName("currency")
        private String currency;
        @SerializedName("goods_detail")
        private List<GoodsDetail> goodsDetails;

        public String getCouponId() {
            return this.couponId;
        }

        public String getName() {
            return this.name;
        }

        public String getScope() {
            return this.scope;
        }

        public String getType() {
            return this.type;
        }

        public Integer getAmount() {
            return this.amount;
        }

        public String getStockId() {
            return this.stockId;
        }

        public Integer getWechatpayContribute() {
            return this.wechatpayContribute;
        }

        public Integer getMerchantContribute() {
            return this.merchantContribute;
        }

        public Integer getOtherContribute() {
            return this.otherContribute;
        }

        public String getCurrency() {
            return this.currency;
        }

        public List<WxPayOrderNotifyV3Result.GoodsDetail> getGoodsDetails() {
            return this.goodsDetails;
        }

        public void setCouponId(String couponId) {
            this.couponId = couponId;
        }

        public void setName(String name) {
            this.name = name;
        }

        public void setScope(String scope) {
            this.scope = scope;
        }

        public void setType(String type) {
            this.type = type;
        }

        public void setAmount(Integer amount) {
            this.amount = amount;
        }

        public void setStockId(String stockId) {
            this.stockId = stockId;
        }

        public void setWechatpayContribute(Integer wechatpayContribute) {
            this.wechatpayContribute = wechatpayContribute;
        }

        public void setMerchantContribute(Integer merchantContribute) {
            this.merchantContribute = merchantContribute;
        }

        public void setOtherContribute(Integer otherContribute) {
            this.otherContribute = otherContribute;
        }

        public void setCurrency(String currency) {
            this.currency = currency;
        }

        public void setGoodsDetails(List<WxPayOrderNotifyV3Result.GoodsDetail> goodsDetails) {
            this.goodsDetails = goodsDetails;
        }

        public boolean equals(Object o) {
            if (o == this) {
                return true;
            } else if (!(o instanceof WxPayOrderNotifyV3Result.PromotionDetail)) {
                return false;
            } else {
                WxPayOrderNotifyV3Result.PromotionDetail other = (WxPayOrderNotifyV3Result.PromotionDetail)o;
                if (!other.canEqual(this)) {
                    return false;
                } else {
                    label143: {
                        Object this$amount = this.getAmount();
                        Object other$amount = other.getAmount();
                        if (this$amount == null) {
                            if (other$amount == null) {
                                break label143;
                            }
                        } else if (this$amount.equals(other$amount)) {
                            break label143;
                        }

                        return false;
                    }

                    Object this$wechatpayContribute = this.getWechatpayContribute();
                    Object other$wechatpayContribute = other.getWechatpayContribute();
                    if (this$wechatpayContribute == null) {
                        if (other$wechatpayContribute != null) {
                            return false;
                        }
                    } else if (!this$wechatpayContribute.equals(other$wechatpayContribute)) {
                        return false;
                    }

                    Object this$merchantContribute = this.getMerchantContribute();
                    Object other$merchantContribute = other.getMerchantContribute();
                    if (this$merchantContribute == null) {
                        if (other$merchantContribute != null) {
                            return false;
                        }
                    } else if (!this$merchantContribute.equals(other$merchantContribute)) {
                        return false;
                    }

                    label122: {
                        Object this$otherContribute = this.getOtherContribute();
                        Object other$otherContribute = other.getOtherContribute();
                        if (this$otherContribute == null) {
                            if (other$otherContribute == null) {
                                break label122;
                            }
                        } else if (this$otherContribute.equals(other$otherContribute)) {
                            break label122;
                        }

                        return false;
                    }

                    label115: {
                        Object this$couponId = this.getCouponId();
                        Object other$couponId = other.getCouponId();
                        if (this$couponId == null) {
                            if (other$couponId == null) {
                                break label115;
                            }
                        } else if (this$couponId.equals(other$couponId)) {
                            break label115;
                        }

                        return false;
                    }

                    Object this$name = this.getName();
                    Object other$name = other.getName();
                    if (this$name == null) {
                        if (other$name != null) {
                            return false;
                        }
                    } else if (!this$name.equals(other$name)) {
                        return false;
                    }

                    Object this$scope = this.getScope();
                    Object other$scope = other.getScope();
                    if (this$scope == null) {
                        if (other$scope != null) {
                            return false;
                        }
                    } else if (!this$scope.equals(other$scope)) {
                        return false;
                    }

                    label94: {
                        Object this$type = this.getType();
                        Object other$type = other.getType();
                        if (this$type == null) {
                            if (other$type == null) {
                                break label94;
                            }
                        } else if (this$type.equals(other$type)) {
                            break label94;
                        }

                        return false;
                    }

                    label87: {
                        Object this$stockId = this.getStockId();
                        Object other$stockId = other.getStockId();
                        if (this$stockId == null) {
                            if (other$stockId == null) {
                                break label87;
                            }
                        } else if (this$stockId.equals(other$stockId)) {
                            break label87;
                        }

                        return false;
                    }

                    Object this$currency = this.getCurrency();
                    Object other$currency = other.getCurrency();
                    if (this$currency == null) {
                        if (other$currency != null) {
                            return false;
                        }
                    } else if (!this$currency.equals(other$currency)) {
                        return false;
                    }

                    Object this$goodsDetails = this.getGoodsDetails();
                    Object other$goodsDetails = other.getGoodsDetails();
                    if (this$goodsDetails == null) {
                        if (other$goodsDetails != null) {
                            return false;
                        }
                    } else if (!this$goodsDetails.equals(other$goodsDetails)) {
                        return false;
                    }

                    return true;
                }
            }
        }

        protected boolean canEqual(Object other) {
            return other instanceof WxPayOrderNotifyV3Result.PromotionDetail;
        }


        public String toString() {
            return "WxPayOrderNotifyV3Result.PromotionDetail(couponId=" + this.getCouponId() + ", name=" + this.getName() + ", scope=" + this.getScope() + ", type=" + this.getType() + ", amount=" + this.getAmount() + ", stockId=" + this.getStockId() + ", wechatpayContribute=" + this.getWechatpayContribute() + ", merchantContribute=" + this.getMerchantContribute() + ", otherContribute=" + this.getOtherContribute() + ", currency=" + this.getCurrency() + ", goodsDetails=" + this.getGoodsDetails() + ")";
        }

        public PromotionDetail() {
        }
    }

    public static class SceneInfo implements Serializable {
        private static final long serialVersionUID = 1L;
        @SerializedName("device_id")
        private String deviceId;

        public String getDeviceId() {
            return this.deviceId;
        }

        public void setDeviceId(String deviceId) {
            this.deviceId = deviceId;
        }

        public boolean equals(Object o) {
            if (o == this) {
                return true;
            } else if (!(o instanceof WxPayOrderNotifyV3Result.SceneInfo)) {
                return false;
            } else {
                WxPayOrderNotifyV3Result.SceneInfo other = (WxPayOrderNotifyV3Result.SceneInfo)o;
                if (!other.canEqual(this)) {
                    return false;
                } else {
                    Object this$deviceId = this.getDeviceId();
                    Object other$deviceId = other.getDeviceId();
                    if (this$deviceId == null) {
                        if (other$deviceId != null) {
                            return false;
                        }
                    } else if (!this$deviceId.equals(other$deviceId)) {
                        return false;
                    }

                    return true;
                }
            }
        }

        protected boolean canEqual(Object other) {
            return other instanceof WxPayOrderNotifyV3Result.SceneInfo;
        }



        public String toString() {
            return "WxPayOrderNotifyV3Result.SceneInfo(deviceId=" + this.getDeviceId() + ")";
        }

        public SceneInfo() {
        }
    }

    public static class Amount implements Serializable {
        private static final long serialVersionUID = 1L;
        @SerializedName("total")
        private Integer total;
        @SerializedName("payer_total")
        private Integer payerTotal;
        @SerializedName("currency")
        private String currency;
        @SerializedName("payer_currency")
        private String payerCurrency;

        public Integer getTotal() {
            return this.total;
        }

        public Integer getPayerTotal() {
            return this.payerTotal;
        }

        public String getCurrency() {
            return this.currency;
        }

        public String getPayerCurrency() {
            return this.payerCurrency;
        }

        public void setTotal(Integer total) {
            this.total = total;
        }

        public void setPayerTotal(Integer payerTotal) {
            this.payerTotal = payerTotal;
        }

        public void setCurrency(String currency) {
            this.currency = currency;
        }

        public void setPayerCurrency(String payerCurrency) {
            this.payerCurrency = payerCurrency;
        }

        public boolean equals(Object o) {
            if (o == this) {
                return true;
            } else if (!(o instanceof WxPayOrderNotifyV3Result.Amount)) {
                return false;
            } else {
                WxPayOrderNotifyV3Result.Amount other = (WxPayOrderNotifyV3Result.Amount)o;
                if (!other.canEqual(this)) {
                    return false;
                } else {
                    label59: {
                        Object this$total = this.getTotal();
                        Object other$total = other.getTotal();
                        if (this$total == null) {
                            if (other$total == null) {
                                break label59;
                            }
                        } else if (this$total.equals(other$total)) {
                            break label59;
                        }

                        return false;
                    }

                    Object this$payerTotal = this.getPayerTotal();
                    Object other$payerTotal = other.getPayerTotal();
                    if (this$payerTotal == null) {
                        if (other$payerTotal != null) {
                            return false;
                        }
                    } else if (!this$payerTotal.equals(other$payerTotal)) {
                        return false;
                    }

                    Object this$currency = this.getCurrency();
                    Object other$currency = other.getCurrency();
                    if (this$currency == null) {
                        if (other$currency != null) {
                            return false;
                        }
                    } else if (!this$currency.equals(other$currency)) {
                        return false;
                    }

                    Object this$payerCurrency = this.getPayerCurrency();
                    Object other$payerCurrency = other.getPayerCurrency();
                    if (this$payerCurrency == null) {
                        if (other$payerCurrency != null) {
                            return false;
                        }
                    } else if (!this$payerCurrency.equals(other$payerCurrency)) {
                        return false;
                    }

                    return true;
                }
            }
        }

        protected boolean canEqual(Object other) {
            return other instanceof WxPayOrderNotifyV3Result.Amount;
        }


        public String toString() {
            return "WxPayOrderNotifyV3Result.Amount(total=" + this.getTotal() + ", payerTotal=" + this.getPayerTotal() + ", currency=" + this.getCurrency() + ", payerCurrency=" + this.getPayerCurrency() + ")";
        }

        public Amount() {
        }
    }

    public static class Payer implements Serializable {
        private static final long serialVersionUID = 1L;
        @SerializedName("openid")
        private String openid;

        public String getOpenid() {
            return this.openid;
        }

        public void setOpenid(String openid) {
            this.openid = openid;
        }

        public boolean equals(Object o) {
            if (o == this) {
                return true;
            } else if (!(o instanceof WxPayOrderNotifyV3Result.Payer)) {
                return false;
            } else {
                WxPayOrderNotifyV3Result.Payer other = (WxPayOrderNotifyV3Result.Payer)o;
                if (!other.canEqual(this)) {
                    return false;
                } else {
                    Object this$openid = this.getOpenid();
                    Object other$openid = other.getOpenid();
                    if (this$openid == null) {
                        if (other$openid != null) {
                            return false;
                        }
                    } else if (!this$openid.equals(other$openid)) {
                        return false;
                    }

                    return true;
                }
            }
        }

        protected boolean canEqual(Object other) {
            return other instanceof WxPayOrderNotifyV3Result.Payer;
        }



        public String toString() {
            return "WxPayOrderNotifyV3Result.Payer(openid=" + this.getOpenid() + ")";
        }

        public Payer() {
        }
    }


    @Data
    public static class DecryptNotifyResult implements Serializable {
        private static final long serialVersionUID = 1L;
        @SerializedName("appid")
        private String appid;
        @SerializedName("mchid")
        private String mchid;
        @SerializedName("out_trade_no")
        private String outTradeNo;
        @SerializedName("transaction_id")
        private String transactionId;
        @SerializedName("trade_type")
        private String tradeType;
        @SerializedName("trade_state")
        private String tradeState;
        @SerializedName("trade_state_desc")
        private String tradeStateDesc;
        @SerializedName("bank_type")
        private String bankType;
        @SerializedName("attach")
        private String attach;
        @SerializedName("success_time")
        private String successTime;
        private WxPayOrderNotifyV3Result.Payer payer;
        @SerializedName("amount")
        private WxPayOrderNotifyV3Result.Amount amount;
        @SerializedName("scene_info")
        private WxPayOrderNotifyV3Result.SceneInfo sceneInfo;
        @SerializedName("promotion_detail")
        private List<WxPayOrderNotifyV3Result.PromotionDetail> promotionDetails;

        public String getAppid() {
            return this.appid;
        }

        public String getMchid() {
            return this.mchid;
        }

        public String getOutTradeNo() {
            return this.outTradeNo;
        }

        public String getTransactionId() {
            return this.transactionId;
        }

        public String getTradeType() {
            return this.tradeType;
        }

        public String getTradeState() {
            return this.tradeState;
        }

        public String getTradeStateDesc() {
            return this.tradeStateDesc;
        }

        public String getBankType() {
            return this.bankType;
        }

        public String getAttach() {
            return this.attach;
        }

        public String getSuccessTime() {
            return this.successTime;
        }

        public WxPayOrderNotifyV3Result.Payer getPayer() {
            return this.payer;
        }

        public WxPayOrderNotifyV3Result.Amount getAmount() {
            return this.amount;
        }

        public WxPayOrderNotifyV3Result.SceneInfo getSceneInfo() {
            return this.sceneInfo;
        }

        public List<WxPayOrderNotifyV3Result.PromotionDetail> getPromotionDetails() {
            return this.promotionDetails;
        }

        public void setAppid(String appid) {
            this.appid = appid;
        }

        public void setMchid(String mchid) {
            this.mchid = mchid;
        }

        public void setOutTradeNo(String outTradeNo) {
            this.outTradeNo = outTradeNo;
        }

        public void setTransactionId(String transactionId) {
            this.transactionId = transactionId;
        }

        public void setTradeType(String tradeType) {
            this.tradeType = tradeType;
        }

        public void setTradeState(String tradeState) {
            this.tradeState = tradeState;
        }

        public void setTradeStateDesc(String tradeStateDesc) {
            this.tradeStateDesc = tradeStateDesc;
        }

        public void setBankType(String bankType) {
            this.bankType = bankType;
        }

        public void setAttach(String attach) {
            this.attach = attach;
        }

        public void setSuccessTime(String successTime) {
            this.successTime = successTime;
        }

        public void setPayer(WxPayOrderNotifyV3Result.Payer payer) {
            this.payer = payer;
        }

        public void setAmount(WxPayOrderNotifyV3Result.Amount amount) {
            this.amount = amount;
        }

        public void setSceneInfo(WxPayOrderNotifyV3Result.SceneInfo sceneInfo) {
            this.sceneInfo = sceneInfo;
        }

        public void setPromotionDetails(List<WxPayOrderNotifyV3Result.PromotionDetail> promotionDetails) {
            this.promotionDetails = promotionDetails;
        }

        public boolean equals(Object o) {
            if (o == this) {
                return true;
            } else if (!(o instanceof WxPayOrderNotifyV3Result.DecryptNotifyResult)) {
                return false;
            } else {
                WxPayOrderNotifyV3Result.DecryptNotifyResult other = (WxPayOrderNotifyV3Result.DecryptNotifyResult)o;
                if (!other.canEqual(this)) {
                    return false;
                } else {
                    Object this$appid = this.getAppid();
                    Object other$appid = other.getAppid();
                    if (this$appid == null) {
                        if (other$appid != null) {
                            return false;
                        }
                    } else if (!this$appid.equals(other$appid)) {
                        return false;
                    }

                    Object this$mchid = this.getMchid();
                    Object other$mchid = other.getMchid();
                    if (this$mchid == null) {
                        if (other$mchid != null) {
                            return false;
                        }
                    } else if (!this$mchid.equals(other$mchid)) {
                        return false;
                    }

                    Object this$outTradeNo = this.getOutTradeNo();
                    Object other$outTradeNo = other.getOutTradeNo();
                    if (this$outTradeNo == null) {
                        if (other$outTradeNo != null) {
                            return false;
                        }
                    } else if (!this$outTradeNo.equals(other$outTradeNo)) {
                        return false;
                    }

                    label158: {
                        Object this$transactionId = this.getTransactionId();
                        Object other$transactionId = other.getTransactionId();
                        if (this$transactionId == null) {
                            if (other$transactionId == null) {
                                break label158;
                            }
                        } else if (this$transactionId.equals(other$transactionId)) {
                            break label158;
                        }

                        return false;
                    }

                    label151: {
                        Object this$tradeType = this.getTradeType();
                        Object other$tradeType = other.getTradeType();
                        if (this$tradeType == null) {
                            if (other$tradeType == null) {
                                break label151;
                            }
                        } else if (this$tradeType.equals(other$tradeType)) {
                            break label151;
                        }

                        return false;
                    }

                    Object this$tradeState = this.getTradeState();
                    Object other$tradeState = other.getTradeState();
                    if (this$tradeState == null) {
                        if (other$tradeState != null) {
                            return false;
                        }
                    } else if (!this$tradeState.equals(other$tradeState)) {
                        return false;
                    }

                    label137: {
                        Object this$tradeStateDesc = this.getTradeStateDesc();
                        Object other$tradeStateDesc = other.getTradeStateDesc();
                        if (this$tradeStateDesc == null) {
                            if (other$tradeStateDesc == null) {
                                break label137;
                            }
                        } else if (this$tradeStateDesc.equals(other$tradeStateDesc)) {
                            break label137;
                        }

                        return false;
                    }

                    label130: {
                        Object this$bankType = this.getBankType();
                        Object other$bankType = other.getBankType();
                        if (this$bankType == null) {
                            if (other$bankType == null) {
                                break label130;
                            }
                        } else if (this$bankType.equals(other$bankType)) {
                            break label130;
                        }

                        return false;
                    }

                    Object this$attach = this.getAttach();
                    Object other$attach = other.getAttach();
                    if (this$attach == null) {
                        if (other$attach != null) {
                            return false;
                        }
                    } else if (!this$attach.equals(other$attach)) {
                        return false;
                    }

                    Object this$successTime = this.getSuccessTime();
                    Object other$successTime = other.getSuccessTime();
                    if (this$successTime == null) {
                        if (other$successTime != null) {
                            return false;
                        }
                    } else if (!this$successTime.equals(other$successTime)) {
                        return false;
                    }

                    label109: {
                        Object this$payer = this.getPayer();
                        Object other$payer = other.getPayer();
                        if (this$payer == null) {
                            if (other$payer == null) {
                                break label109;
                            }
                        } else if (this$payer.equals(other$payer)) {
                            break label109;
                        }

                        return false;
                    }

                    label102: {
                        Object this$amount = this.getAmount();
                        Object other$amount = other.getAmount();
                        if (this$amount == null) {
                            if (other$amount == null) {
                                break label102;
                            }
                        } else if (this$amount.equals(other$amount)) {
                            break label102;
                        }

                        return false;
                    }

                    Object this$sceneInfo = this.getSceneInfo();
                    Object other$sceneInfo = other.getSceneInfo();
                    if (this$sceneInfo == null) {
                        if (other$sceneInfo != null) {
                            return false;
                        }
                    } else if (!this$sceneInfo.equals(other$sceneInfo)) {
                        return false;
                    }

                    Object this$promotionDetails = this.getPromotionDetails();
                    Object other$promotionDetails = other.getPromotionDetails();
                    if (this$promotionDetails == null) {
                        if (other$promotionDetails != null) {
                            return false;
                        }
                    } else if (!this$promotionDetails.equals(other$promotionDetails)) {
                        return false;
                    }

                    return true;
                }
            }
        }

        protected boolean canEqual(Object other) {
            return other instanceof WxPayOrderNotifyV3Result.DecryptNotifyResult;
        }



        public String toString() {
            return "WxPayOrderNotifyV3Result.DecryptNotifyResult(appid=" + this.getAppid() + ", mchid=" + this.getMchid() + ", outTradeNo=" + this.getOutTradeNo() + ", transactionId=" + this.getTransactionId() + ", tradeType=" + this.getTradeType() + ", tradeState=" + this.getTradeState() + ", tradeStateDesc=" + this.getTradeStateDesc() + ", bankType=" + this.getBankType() + ", attach=" + this.getAttach() + ", successTime=" + this.getSuccessTime() + ", payer=" + this.getPayer() + ", amount=" + this.getAmount() + ", sceneInfo=" + this.getSceneInfo() + ", promotionDetails=" + this.getPromotionDetails() + ")";
        }

        public DecryptNotifyResult() {
        }
    }
}
