package com.dicomclub.payment.module.pay.service.nanjing;

import com.alibaba.fastjson.JSON;
import com.dicomclub.payment.common.utils.HttpClientUtil;
import com.dicomclub.payment.module.pay.common.ChannelStateRes;
import com.dicomclub.payment.module.pay.config.NjPayConfig;
import com.dicomclub.payment.module.pay.config.PayConfig;
import com.dicomclub.payment.module.pay.model.*;
import com.dicomclub.payment.module.pay.model.wxpay.BillRequest;
import com.dicomclub.payment.module.pay.model.wxpay.BillResponse;
import com.dicomclub.payment.module.pay.model.wxpay.DivisionReceiverBind;
import com.dicomclub.payment.module.pay.service.PayStrategy;
import com.dicomclub.payment.module.pay.service.nanjing.model.NJReq;
import com.dicomclub.payment.module.pay.service.nanjing.model.NJRes;
import com.dicomclub.payment.util.DateUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.HashMap;

/**
 * @author ftm
 * @date 2023/6/30 0030 16:50
 */
@Slf4j
@Component
@Primary
@Data
public class NanjingPayStrategy extends PayStrategy {

    /**
     * 订单预支付
     *
     * @param payRequest
     * @param payConfig
     * @return
     */
    @Override
    public PayResponse pay(PayRequest payRequest, PayConfig payConfig) {
//        NjPayConfig njPayConfig = (NjPayConfig)payConfig;
//        NJReq njReq = getCommonParam(njPayConfig);
//
//
//        HashMap<String, Object> map = new HashMap<>();
//        map.put("MerchantId", njPayConfig.getMerchantId()); //商户编号
//        map.put("MerUserId", ); //买方会员编号
//        map.put("MerUserAcctNo", ); //买方交易 子账号
//        map.put("MerSellerId", ); //卖方会员 编号
//        map.put("MerSellerAcctNo", ); //卖方交易 子账号
//        map.put("MerchantSeqNo", payRequest.getOrderNo()); //交易流水 号
//        map.put("MerchantDateTime", DateUtils.formatDate(new Date(),DateUtils.YYYYMMDDHHMMSS )); //交易时间
//        map.put("TransType", payRequest.getPayChannel().getCode()); //支付类型
//        map.put("FeeAmount", );//手续费金 额
//        map.put("TransAmount", );//交易金额
//
//
//        String json = HttpClientUtil.doPost(, , );
//        NJRes njRes = JSON.parseObject(json, NJRes.class);


        return null;
    }


    /**
     * 异步通知
     *
     * @param request
     * @param payConfig
     * @return
     */
    @Override
    public PayResponse asyncNotify(HttpServletRequest request, PayConfig payConfig) {
        return null;
    }

    /**
     * 异步通知-退款
     * 与支付结果不同的是，退款结果可能会受到更多的影响因素，比如退款金额、退款原因、退款渠道等，因此退款处理的时间可能会比支付处理更长。通过异步回调接口，商户可以及时获取退款结果，以便及时处理相关业务。
     *
     * @param request
     * @param payConfig
     * @return
     */
    @Override
    public RefundResponse asyncNotifyRefund(HttpServletRequest request, PayConfig payConfig) {
        return null;
    }

    /**
     * 订单结果查询
     *
     * @param request
     * @param payConfig
     */
    @Override
    public PayResponse query(OrderQueryRequest request, PayConfig payConfig) {
        return null;
    }

    /**
     * 退款
     *
     * @param request
     * @param payConfig
     */
    @Override
    public RefundResponse refund(RefundRequest request, PayConfig payConfig) {
        return null;
    }

    /**
     * 退款查询
     *
     * @param refundNo
     * @param payConfig
     */
    @Override
    public RefundResponse refundQuery(RefundQueryRequest refundNo, PayConfig payConfig) {
        return null;
    }

    /**
     * 转账
     *
     * @param order
     * @param payConfig
     */
    @Override
    public TransferResponse transfer(TransferOrder order, PayConfig payConfig) {
        return null;
    }

    @Override
    public TransferResponse transferQuery(TransferQueryRequest request, PayConfig payConfig) {
        return null;
    }

    @Override
    public SettleResponse settle(SettleRequest settleRequest, PayConfig payConfig) {
        return null;
    }

    /**
     * 解冻 ; 调用分账接口后，需要解冻剩余资金时，调用本接口将剩余的分账金额全部解冻给本商户
     *
     * @param unfreezeRequest
     * @param payConfig
     */
    @Override
    public ChannelStateRes unfreeze(UnfreezeRequest unfreezeRequest, PayConfig payConfig) {
        return null;
    }

    /**
     * 分账
     *
     * @param request
     * @param payConfig
     */
    @Override
    public DivisionResponse division(DivisionRquest request, PayConfig payConfig) {
        return null;
    }

    @Override
    public DivisionResponse divisionQuery(DivisionQueryRquest divisionQueryRquest, PayConfig payConfig) {
        return null;
    }

    @Override
    public ChannelStateRes divisionBind(DivisionReceiverBind divisionReceiverBind, PayConfig payConfig) {
        return null;
    }

    @Override
    public BillResponse downloadBill(BillRequest downloadBillRequest, PayConfig payConfig) {
        return null;
    }

    @Override
    public VirtualAccountApplyRes virtualAccountApply(VirtualAccountApplyReq virtualAccountApplyReq, PayConfig payConfig) {

        VirtualAccountApplyRes res = new VirtualAccountApplyRes();
        res.setChannelStateRes(ChannelStateRes.waiting());
        return res;
    }

    private NJReq getCommonParam(NjPayConfig payConfig) {
        NJReq njReq = new NJReq();
        NJReq.ReqHead reqHead = new NJReq.ReqHead();
        njReq.setAppID(payConfig.getAppId());
        return njReq;
    }


}
