# 介绍
spay-boot-starter目前已支持支付宝，银联，微信V3/v2各渠道支付）
支持单商户，多商户调用

(交流VX:tyb1632414557)

# 使用教程
- 引入依赖(pom.xml)

最新版请到 <https://mvnrepository.com/artifact/io.github.tyb-luoyun/spay-spring-boot-starter> 查看
```
<dependency>
   <groupId>io.github.tyb-luoyun</groupId>
   <artifactId>spay-spring-boot-starter</artifactId>
   <version>1.1.1</version> 
</dependency>
```

## 统一支付(多商户)
- 调用示例
``` 
   PayChannel payChannel = PayChannel.UNION_WEB_GATEWAY;//这里渠道选择了银联网关支付
   PayConfig payConfig = payProcessService.getPayConfig(payChannel,getNotifyUrl(order.getPayOrderId()),order.getAppId());//这个配置根据商户和支付方式有所不同,需要自己完善
   PayRequest payRequest = getPayRequest(payChannel , payOrderVo,order,payConfig);//获取订单封装实体，需要自己完善
   PayStrategy patStrategy = PayContext.getPayStrategy(payChannel);//获取支付策略
   PayResponse payResponse = patStrategy.pay(payRequest, payConfig);//进行支付
```

## 单个支付(单商户)
- 基础支付环境配置 (针对单商户-此处以微信V3支付为例)
```
@Configuration
public class PayConfig {
    @Bean
    public WxPayConfig wxPayConfig(){
        WxPayConfig wxPayConfig = new WxPayConfig();
        wxPayConfig.setAppId("-----");
        wxPayConfig.setMchId("-----");
        wxPayConfig.setAppSecret("------");
        wxPayConfig.setPrivateKey("------");
        wxPayConfig.setNotifyUrl("https://---");
        wxPayConfig.setReturnUrl("https://---");
        wxPayConfig.setInputCharset("utf-8");
        wxPayConfig.setApiClientKeyP12("D:\\----.p12");
        wxPayConfig.setCertStoreType(CertStoreType.PATH);
        return wxPayConfig;
    }
}
```
- 调用单个支付(以微信为例)
```
    @Autowire
    private WxPayStrategy wxV3PayService;
```
``` 
    WxPayRequest wxPayRequest = new WxPayRequest();
    wxPayRequest.setOrderName("订单title");
    wxPayRequest.setOrderDesc("摘要");
    wxPayRequest.setOrderAmount(BigDecimal.valueOf(0.01));
    wxPayRequest.setOrderNo("ieh489327ndu73nmd4332"); 
    wxPayRequest.setPayChannel(PayChannel.WXPAY_MINI);  
    wxPayRequest.setOpenid("oF1NI5F9aMkQa2TX_NAC7cBnuIB8");

    PayResponse pay = wxV3PayService.pay(wxPayRequest,null);         
```

