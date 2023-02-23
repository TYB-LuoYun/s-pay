package com.dicomclub.payment.module.pay.service.wxpay.channel;

import com.dicomclub.payment.module.pay.config.PayConfig;
import com.dicomclub.payment.module.pay.config.WxPayConfig;
import com.dicomclub.payment.module.pay.model.PayRequest;
import com.dicomclub.payment.module.pay.model.PayResponse;
import com.dicomclub.payment.module.pay.model.wxpay.WxPayApi;
import com.dicomclub.payment.module.pay.model.wxpay.WxPayRequest;
import com.dicomclub.payment.module.pay.model.wxpay.request.WxPayUnifiedorderRequest;
import com.dicomclub.payment.module.pay.model.wxpay.response.WxPaySyncResponse;
import com.dicomclub.payment.module.pay.service.wxpay.WxPayStrategy;
import com.dicomclub.payment.module.pay.service.wxpay.common.WxPaySignature;
import com.dicomclub.payment.util.MapUtil;
import com.dicomclub.payment.util.MoneyUtil;
import com.dicomclub.payment.util.RandomUtil;
import com.dicomclub.payment.util.XmlUtil;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import retrofit2.Call;
import retrofit2.Response;

import java.io.IOException;

/**
 * @author ftm
 * @date 2023/2/17 0017 16:30
 */
@Component
public class WxPayMicroService extends WxPayStrategy {


    /**
     * 微信付款码支付
     * 提交支付请求后微信会同步返回支付结果。
     * 当返回结果为“系统错误 {@link WxPayConstants#SYSTEMERROR}”时，商户系统等待5秒后调用【查询订单API {@link WxPayServiceImpl#query(OrderQueryRequest)}】，查询支付实际交易结果；
     * 当返回结果为“正在支付 {@link WxPayConstants#USERPAYING}”时，商户系统可设置间隔时间(建议10秒)重新查询支付结果，直到支付成功或超时(建议30秒)；
     *
     * @return
     */
    @Override
    public PayResponse pay(PayRequest payRequest, PayConfig payConfig) {
        WxPayConfig wxPayConfig = (WxPayConfig)payConfig;
        WxPayRequest request = (WxPayRequest)payRequest;
        WxPayUnifiedorderRequest wxRequest = new WxPayUnifiedorderRequest();
        wxRequest.setOutTradeNo(request.getOrderNo());
        wxRequest.setTotalFee(MoneyUtil.Yuan2Fen(request.getOrderAmount()));
        wxRequest.setBody(request.getOrderName());
        wxRequest.setOpenid(request.getOpenid());
        wxRequest.setAuthCode(request.getAuthCode());

        wxRequest.setAppid(wxPayConfig.getAppId());
        wxRequest.setMchId(wxPayConfig.getMchId());
        wxRequest.setNonceStr(RandomUtil.getRandomStr());
        wxRequest.setSpbillCreateIp(StringUtils.isEmpty(request.getSpbillCreateIp()) ? "8.8.8.8" : request.getSpbillCreateIp());
        wxRequest.setAttach(request.getAttach());
        wxRequest.setSign(WxPaySignature.sign(MapUtil.buildMap(wxRequest), wxPayConfig.getMchKey()));

        //对付款码支付无用的字段
        wxRequest.setNotifyUrl("");
        wxRequest.setTradeType("");

        RequestBody body = RequestBody.create(MediaType.parse("application/xml; charset=utf-8"), XmlUtil.toString(wxRequest));

        WxPayApi api = null;
        if (wxPayConfig.isSandbox()) {
            api = devRetrofit.create(WxPayApi.class);
        } else {
            api = retrofit.create(WxPayApi.class);
        }

        Call<WxPaySyncResponse> call = api.micropay(body);

        Response<WxPaySyncResponse> retrofitResponse = null;
        try {
            retrofitResponse = call.execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
        assert retrofitResponse != null;
        if (!retrofitResponse.isSuccessful()) {
            throw new RuntimeException("【微信付款码支付】发起支付, 网络异常");
        }
        return buildPayResponse(retrofitResponse.body(),wxPayConfig);
    }
}
