package com.dicomclub.payment.module.pay.demo;

import com.dicomclub.payment.module.pay.config.AliPayConfig;
import com.dicomclub.payment.module.pay.config.UnionPayConfig;
import com.dicomclub.payment.module.pay.enums.PayChannel;
import com.dicomclub.payment.module.pay.model.OrderQueryRequest;
import com.dicomclub.payment.module.pay.model.PayResponse;
import com.dicomclub.payment.module.pay.model.alipay.AliPayRequest;
import com.dicomclub.payment.module.pay.model.union.UnionPayRequest;
import com.dicomclub.payment.module.pay.service.PayStrategy;
import com.dicomclub.payment.module.pay.service.alipay.AliPayStrategy;
import com.dicomclub.payment.module.pay.service.union.UnionPayStrategy;
import com.dicomclub.payment.util.httpRequest.CertStoreType;

import java.math.BigDecimal;

/**
 * @author ftm
 * @date 2023/5/23 0023 10:21
 */
public class DemoController {


    private static void testAliPay() {
        AliPayStrategy payStrategy = new AliPayStrategy();
        AliPayRequest aliPayRequest = new AliPayRequest();
        AliPayConfig config = new AliPayConfig();
        config.setAppId("2016103000778633");
        config.setPrivateKey("MIIEvwIBADANBgkqhkiG9w0BAQEFAASCBKkwggSlAgEAAoIBAQC9PiyNWCfQQvKGd47ZDnL1In1qWtZlz2Mb72SU78wdajXw8H50PtEAaxyXC1ZQwV+S8RNwyWu9ps0ZfytLsc5Fkfus0NFoKLEgRR0AsPMiO8VDqAb/g8JaJxXjSEvK7YkPzwi9gmOQZrSZ+6Z6Aonx5GGFsrELhMtCAjNaFULmxmSDCBIt80Xz2UMCTzRHoCvWk6G13bfib6HZhBXDEKMnQENUvmUP6uxmQyhJfQlFnoEDTwvwQeoW4gZST0hp3qSP2IA/M+oOCGY80uB5XtDsTgxOfAZyZBfQWuKCuo0IkW8nlrUB3FhT3SZvi5Ly+Nm/GY0p+zEm30n7ILDPujvdAgMBAAECggEBAITBlBGH8U4pWOe6Aa0FdOHA00iD9vmvFfDn7KCK5J8R/ktK+vrjgk0P1xui6Wd3cGut0RyDQKDn3ePwEVhloLWvZmXii7TNLwzbTzBH8hhm48jFnDstnG8QEkvCUOFH56n+bDw9t0j6s+F951iaZVkbBMr9AzR4cPqafpnIZolrF8UJQRVha7/A0kJlEkHhfmKtx2MuJlqETW3zEdotVuHWGmXkGUsY+SGKYNcFi3VIQd0dLPLo7PikWegCXlzdfHXi2Y7KQeBqH65kPqtDAsgdk9JlLqu9e3PfHxtSsvLNYDRiTLcIEVVyfrpkxMRcZalXpjS/9FFIQpizrbFoNuUCgYEA8TUjAVSrz5Fci+yfFwOW/HxOcIrDgUjEflQzolufUnFdAv3ILVyxd+BXDzyhZF9FzFPXsn8uCj2FR8P1TuOY3iM+V0Izs+DnucSSY/VW2FvXi9p/jwILuBTEC6PXQFbn+MydqBO3N/cPwWUgYWt9WNm/pvr11nfpbXYZp6y+uL8CgYEAyNk2TN2E86tSet/E0Dwlpfo+uK1oTg+mfsFJev9urmDmUXCW8xCYyazog+4xB83MNm8/V/ihtCyWyk2UgvbTBd3hfKGBJWrt+AAF1fM2y0hbkve20CLF7ZZeLVCufSVj+eagjcnoeZ5bs4lu49ACBWCDb2krUATY6xj2ilSftmMCgYEAvifX6uqncEH2pdDrMqd/1pjg9dRPgJKvZbBX7H0ywznfy8Xqk+hpeEoCGF8CFTEOw6CtgWjGEQijFAqmT4UaNLWwJfZo8Dw0Mr2HcwotZvAwo26j9Uf1mS+1xj9qKKzav8f/2kuAu7woTZy9xE+LSAqSDr/2IxfxjHv4ibmjud0CgYAa2whqsLgFSOQnb+JGIbS7A8H9OZqXzXnquveTTbJD+MrzGsXkTFTRqqAQe/nsXDUxFiD3J1Sf3dJvzH3OISjIQQnUe1fkY+7b2Uzuda3e8pbkCOFV5UBn61I+Zupd63D5yj+vlc7S7DzAveCo73hFtcj9Taev9GX40YatqnEMlwKBgQCeO+4N/7V3f46ju6yz+qpnH0VyAbe6RlVTqqS9IBIMNAuXL1yq8paGKrep8St2jOKfYJC9iOnZwIfWe0A9z4zz6Lslr7eeoj/oFCGRob7eOK1NrHFWxleom7DWZ0wnGHBMkvtwnyRy/8T8FJixAQJqeXWEvrWX1/sK24+A6o8V8w==");
        config.setAliPayPublicKey("MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAkP122miaAWmv8kZISSckvzNXVcM/u6LG4L3ZWHuZNqGbHkhIqvEWHCqS2NVvUet++MksmXPWLpG+NvXLAjczXdsGuDHU3asuHNnTIM/Ah/DASxvf/GY7HlxylSnA0zY4eB3ETb1FDR0J6k7KCV4hl0QuepNDALnuhzqJeJPTM/cGzVvK0IXcOUyWl9mHLbcidHuJEloZBm23tQCDPxbriM6qM+omhTrHG2/RURhBm119RKbzt2QY6KJBMpDC3GXV4hPdFNFKzph1q3GOoEJWWt+KPMy4XlLVzu6+g6mDqELGmkqmoIpzjYU9VV1hqkv4PEMDXkaGnAQP4HsJgvAGswIDAQAB");
        config.setSandbox(true);
        aliPayRequest.setOrderName("name");
        aliPayRequest.setOrderNo("583hcy3hjud3hncisw3j");
        aliPayRequest.setOrderAmount(BigDecimal.valueOf(56));

        aliPayRequest.setPayChannel(PayChannel.ALIPAY_PC);

        PayResponse pay = payStrategy.pay(aliPayRequest, config);
        System.out.println(pay.getPayUrl());
    }

    private static void testAliPay2() {
        AliPayStrategy payStrategy = new AliPayStrategy();
        AliPayRequest aliPayRequest = new AliPayRequest();
        AliPayConfig config = new AliPayConfig();
        config.setAppId("2021000122673060");
        config.setPrivateKey("MIIEvAIBADANBgkqhkiG9w0BAQEFAASCBKYwggSiAgEAAoIBAQDX4BAzDn5VMDBrymU4eUImtdpVoIWcyeVenxb5s7uyu2Ojana4b1HiiFLKWgkDkalrJWeToKw0rCCrWmOnL002Ae7g7iewT32ACBQ7zTXBs2THt4Pj0Uwx6TteJY34MyZ6CB4BnZu5L7jvAQx0Fo7mceGSIX0zyOD1hg0vSDf/sRDoArhQoHslf/cBydxvdzJ6wp+Od3UbxAJYYZ8RWP4fvK9v3npZub5Vs2tDFLVJcMYCWwyWXqSAf4xX2D8PdqAv6NPRbIl4rMsTGyj/Dnaa9z+QutpY7I3bQcKgYRk648qIhNR7Jj1SwxS9zuzQ32iRmtowKMRH/y3vUfUZdtDTAgMBAAECggEAVcLaLK5xWf6O/gOmOs1RjndoieP/sM40pWamhgT1aIgxiVZeW989tLVbzq/+LMDNgZeWknuebj6DrA94z45nKjGSaSGz+sNZpGeRQkDAPne6be1mJDeeAaYaw8g/on8PpiNbaGDo4KUo3yqXe/cKRlqvtpI/XzdKx7+9tS8HsDg5I+x+2mIJnWsJuCrHhjlCfeesHsk9lYoEY2OejPKUEKACl6MQXOHUvkgbt3jzrqss5VEKk19/+nQPqVUdy1mdjobNEtvnZAUVxJc/I0YT6mSgwgJnwhGNXe50hmZ0p0/Ry27+c9t3fzxf3m8JEaqYEnbMBuGPvNYHHM5OABAcwQKBgQDuznNLA4MsCt5UWpBrCoofjQJEIL7sU0jyvYr1cr8KYorqWqCRy9qi5x7P2otv3X3Q0RiLA8Or78H1BnoEuqyS1ArlUERse8sj/+34RJB8eDs5EqLYECnJss4nAFbLnSoggm3XI8CFfx0z1xPjdAROHe/H6RNrx4HFp0Yq8Gj1SwKBgQDnavUlkvTCdvjgzXzbnXmiBSGt60nlGl65ayuYvcdyd/j8G/2GYvrlyNW32SHDNAhloevxB7/eeGUT0svFW9XNkF6BgRuViRa7E9XNKLj69Pz3wdQbkaBvwrTLDIXZOIYri5uhtqLX7tOMtQcvRSSiNYmn1XFkURRldgxkMX1FmQKBgEnLil037KUDhsjSY6ZwT1aIoJak99rXsccxQ2ut1dNDuGHLN6tWL23/tcnNYyFidKq+srLiqujK4kjxg2tKtGF7HRLXxw0vBAtP3x3FMlEEZmiwlZnnBMLLemEa/bdWDdqV85Nz/N5D9aY7ZG35QAtTvPEt2U6JDFUj588FE6oZAoGARLGtP5AV87vZOPIGhDKErqGTU8sqTkW7pJK5iEedcs9GK6Ara77p91fciQx9RzKk43ZjUVMZk3JtnqrOLjGKj9CxHQQ0Kflds/65UoMqFeSvSuRQkDZ0R3imrjdza/2pZje05RYP1MViKrx5+4As1bHKwPVNCZg/07ZtCzjdlNECgYAlzFxAw+ypNKYTJQXJMH9i6t3N4RPo3lJvW70Veqx8hLKG65g7svMgmu+xG56LGPGFTds4wXUq9ZQpQJocJjGyp0ECsXcRggEXbvHDubuAEclBdVL9ZIjQFDAUWuMLnLxAyyEQgqGcdTdzj2wbb/0YjB/xfZrrIU5c8v+U99zhUg==");
        config.setAliPayPublicKey("MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAl+VKyemkmMJuBewxmdGBwmR5AYoA6dtMU702e8fy7wVdgB80ZgIYqem9EOxLAnZDpdGBQvu4S6JPiAUOaHZJK1qO3CQNJKLLBNJF+rqHVlw7Rn+Nl4zzS/uA50ZX/sC1vFHEY9RVu86GOr2A+aWnDhlxXiom17nXKPao4dvky0m2+X2BWwyLyAFRLXoVRWs6ZGpLVdNJd89hMgNuuZCpQl6kdDBvuf1bCwtNPC6w+wPATd1UNwwli9QeA9V1UIHxZ9nbcUJMH3Uc787JeN102dGokcRhLHQuiLPH+/v56KK/T4kp9FvGa89emTR2lZHz0Y7ahO+hXvO8ECN/o8DY8wIDAQAB");
        config.setSandbox(true);
        aliPayRequest.setOrderName("name");
        aliPayRequest.setOrderNo("583hcy3hjud3hncisw3j");
        aliPayRequest.setOrderAmount(BigDecimal.valueOf(56));

        aliPayRequest.setPayChannel(PayChannel.ALIPAY_PC);

        PayResponse pay = payStrategy.pay(aliPayRequest, config);
        System.out.println(pay.getPayUrl());
    }


    /**
     * 证书模式
     */
    private static void testAliPayCert() {
        AliPayStrategy payStrategy = new AliPayStrategy();
        AliPayRequest aliPayRequest = new AliPayRequest();
        AliPayConfig config = new AliPayConfig();
        config.setAppId("2021000122673060");
        config.setPrivateKey("MIIEvAIBADANBgkqhkiG9w0BAQEFAASCBKYwggSiAgEAAoIBAQDX4BAzDn5VMDBrymU4eUImtdpVoIWcyeVenxb5s7uyu2Ojana4b1HiiFLKWgkDkalrJWeToKw0rCCrWmOnL002Ae7g7iewT32ACBQ7zTXBs2THt4Pj0Uwx6TteJY34MyZ6CB4BnZu5L7jvAQx0Fo7mceGSIX0zyOD1hg0vSDf/sRDoArhQoHslf/cBydxvdzJ6wp+Od3UbxAJYYZ8RWP4fvK9v3npZub5Vs2tDFLVJcMYCWwyWXqSAf4xX2D8PdqAv6NPRbIl4rMsTGyj/Dnaa9z+QutpY7I3bQcKgYRk648qIhNR7Jj1SwxS9zuzQ32iRmtowKMRH/y3vUfUZdtDTAgMBAAECggEAVcLaLK5xWf6O/gOmOs1RjndoieP/sM40pWamhgT1aIgxiVZeW989tLVbzq/+LMDNgZeWknuebj6DrA94z45nKjGSaSGz+sNZpGeRQkDAPne6be1mJDeeAaYaw8g/on8PpiNbaGDo4KUo3yqXe/cKRlqvtpI/XzdKx7+9tS8HsDg5I+x+2mIJnWsJuCrHhjlCfeesHsk9lYoEY2OejPKUEKACl6MQXOHUvkgbt3jzrqss5VEKk19/+nQPqVUdy1mdjobNEtvnZAUVxJc/I0YT6mSgwgJnwhGNXe50hmZ0p0/Ry27+c9t3fzxf3m8JEaqYEnbMBuGPvNYHHM5OABAcwQKBgQDuznNLA4MsCt5UWpBrCoofjQJEIL7sU0jyvYr1cr8KYorqWqCRy9qi5x7P2otv3X3Q0RiLA8Or78H1BnoEuqyS1ArlUERse8sj/+34RJB8eDs5EqLYECnJss4nAFbLnSoggm3XI8CFfx0z1xPjdAROHe/H6RNrx4HFp0Yq8Gj1SwKBgQDnavUlkvTCdvjgzXzbnXmiBSGt60nlGl65ayuYvcdyd/j8G/2GYvrlyNW32SHDNAhloevxB7/eeGUT0svFW9XNkF6BgRuViRa7E9XNKLj69Pz3wdQbkaBvwrTLDIXZOIYri5uhtqLX7tOMtQcvRSSiNYmn1XFkURRldgxkMX1FmQKBgEnLil037KUDhsjSY6ZwT1aIoJak99rXsccxQ2ut1dNDuGHLN6tWL23/tcnNYyFidKq+srLiqujK4kjxg2tKtGF7HRLXxw0vBAtP3x3FMlEEZmiwlZnnBMLLemEa/bdWDdqV85Nz/N5D9aY7ZG35QAtTvPEt2U6JDFUj588FE6oZAoGARLGtP5AV87vZOPIGhDKErqGTU8sqTkW7pJK5iEedcs9GK6Ara77p91fciQx9RzKk43ZjUVMZk3JtnqrOLjGKj9CxHQQ0Kflds/65UoMqFeSvSuRQkDZ0R3imrjdza/2pZje05RYP1MViKrx5+4As1bHKwPVNCZg/07ZtCzjdlNECgYAlzFxAw+ypNKYTJQXJMH9i6t3N4RPo3lJvW70Veqx8hLKG65g7svMgmu+xG56LGPGFTds4wXUq9ZQpQJocJjGyp0ECsXcRggEXbvHDubuAEclBdVL9ZIjQFDAUWuMLnLxAyyEQgqGcdTdzj2wbb/0YjB/xfZrrIU5c8v+U99zhUg==");
        config.setAliPayRootCert("D:/1work/项目资料/支付/支付宝测试证书/alipayRootCert.crt");
        config.setAliPayPublicCert("D:/1work/项目资料/支付/支付宝测试证书/alipayPublicCert.crt");
        config.setAppPublicCert("D:/1work/项目资料/支付/支付宝测试证书/appPublicCert.crt");
        config.setCertStoreType(CertStoreType.PATH);
        config.setUseCert(true);
        config.setSandbox(true);
        aliPayRequest.setOrderName("name");
        aliPayRequest.setOrderNo("583hcy3hjud3hncisw3j");
        aliPayRequest.setOrderAmount(BigDecimal.valueOf(56));

        aliPayRequest.setPayChannel(PayChannel.ALIPAY_PC);

        PayResponse pay = payStrategy.pay(aliPayRequest, config);
        System.out.println(pay.getPayUrl());
    }

    public static void main(String[] args){
        testUnionPay();
    }

    public static void testUnionPay(){
        PayStrategy payStrategy = new UnionPayStrategy();
        UnionPayRequest aliPayRequest = new UnionPayRequest();
        UnionPayConfig config = new UnionPayConfig();
        config.setMchId("777290058202371");
        config.setAcpMiddleCert("D:/1Anets.top/code/s-pay/src/main/resources/union/1677569632937acp_test_middle.cer");
        config.setAcpRootCert("D:/1Anets.top/code/s-pay/src/main/resources/union/1677569698412acp_test_root.cer");
        config.setKeyPrivateCert("D:/1Anets.top/code/s-pay/src/main/resources/union/1677566549775acp_test_sign.pfx");
        config.setKeyPrivateCertPwd("000000");
        config.setCertStoreType(CertStoreType.PATH);
        config.setNotifyUrl("http://127.0.0.1");

        config.setSandbox(true);
        aliPayRequest.setOrderName("name");
        aliPayRequest.setOrderNo("583hcy3hjud3hncisw3j");
        aliPayRequest.setOrderAmount(BigDecimal.valueOf(56));

        aliPayRequest.setPayChannel(PayChannel.UNION_QRCODE);

        PayResponse pay = payStrategy.pay(aliPayRequest, config);
        System.out.println(pay.buildPayData());
    }

}
