package com.dicomclub.module.pay.model;

import com.dicomclub.exception.ServiceException;
import com.dicomclub.module.pay.enums.PayChannel;
import com.dicomclub.module.pay.enums.PayType;
import com.dicomclub.module.pay.model.alipay.AliPayRequest;
import com.dicomclub.module.pay.model.wxpay.WxPayRequest;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author ftm
 * @date 2023/2/16 0016 14:24
 *
 *
 * 设计考量：1为什么不同的支付场景实体类要分开？ 便于区分理解
 * 2为什么同一支付场景的不同渠道实体类却要公用？ 由于是同一个场景下不同渠道 字段区分不大，如果 每一个渠道一个实体类的话，那很复杂了
 * 3这里使用的组合模型
 */

public class AllPayRequest {

    private PayChannel payChannel;
    private AliPayRequest aliPayRequest;
    private WxPayRequest wxPayRequest;
    private PayRequest payRequest;
    private String channelCode;

    /**
     * 这个构造方法用来控制顺序
     */
    public AllPayRequest(){

    }
    @JsonCreator
    public AllPayRequest(@JsonProperty("aliPayRequest")AliPayRequest aliPayRequest,  @JsonProperty("wxPayRequest")WxPayRequest wxPayRequest, @JsonProperty("payRequest")PayRequest payRequest, @JsonProperty("channelCode")String channelCode ){
        this.aliPayRequest = aliPayRequest;
        this.wxPayRequest = wxPayRequest;
        this.payRequest = payRequest;
        this.channelCode = channelCode;
        this.payChannel = PayChannel.getByName(this.channelCode);
        if(this.payChannel.getPayType() ==  PayType.ALIPAY){
            this.payRequest = aliPayRequest;
        }else if(payChannel.getPayType() ==  PayType.WX){
            this.payRequest = wxPayRequest;
        }else{
            throw new ServiceException("暂不支持的支付渠道:"+this.channelCode);
        }
        if(this.payRequest == null){
            throw new ServiceException("支付渠道参数与渠道不对应，请检查");
        }
        this.payChannel = payChannel;
    }



    public PayRequest getPayRequest() {
        PayChannel alipayApp = payChannel;
        if(alipayApp.getPayType() == (PayType.ALIPAY)){
            this.aliPayRequest.setPayChannel(alipayApp);
            return this.aliPayRequest;
        }
        return null;
    }


    public PayChannel getPayChannel(){
        return payChannel;
    }









}
