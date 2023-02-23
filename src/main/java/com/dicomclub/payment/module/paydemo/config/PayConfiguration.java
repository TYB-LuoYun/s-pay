package com.dicomclub.payment.module.paydemo.config;

import com.dicomclub.payment.module.pay.config.AliPayConfig;
import com.dicomclub.payment.module.pay.config.WxPayConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author ftm
 * @date 2023/2/17 0017 10:17
 */
@Configuration
public class PayConfiguration {
    @Bean
    public AliPayConfig aliPayConfig(){

//		支付宝支付配置
        AliPayConfig aliPayConfig = new AliPayConfig();
        aliPayConfig.setAppId("2016103000778633");
        aliPayConfig.setPrivateKey("MIIEvwIBADANBgkqhkiG9w0BAQEFAASCBKkwggSlAgEAAoIBAQC9PiyNWCfQQvKGd47ZDnL1In1qWtZlz2Mb72SU78wdajXw8H50PtEAaxyXC1ZQwV+S8RNwyWu9ps0ZfytLsc5Fkfus0NFoKLEgRR0AsPMiO8VDqAb/g8JaJxXjSEvK7YkPzwi9gmOQZrSZ+6Z6Aonx5GGFsrELhMtCAjNaFULmxmSDCBIt80Xz2UMCTzRHoCvWk6G13bfib6HZhBXDEKMnQENUvmUP6uxmQyhJfQlFnoEDTwvwQeoW4gZST0hp3qSP2IA/M+oOCGY80uB5XtDsTgxOfAZyZBfQWuKCuo0IkW8nlrUB3FhT3SZvi5Ly+Nm/GY0p+zEm30n7ILDPujvdAgMBAAECggEBAITBlBGH8U4pWOe6Aa0FdOHA00iD9vmvFfDn7KCK5J8R/ktK+vrjgk0P1xui6Wd3cGut0RyDQKDn3ePwEVhloLWvZmXii7TNLwzbTzBH8hhm48jFnDstnG8QEkvCUOFH56n+bDw9t0j6s+F951iaZVkbBMr9AzR4cPqafpnIZolrF8UJQRVha7/A0kJlEkHhfmKtx2MuJlqETW3zEdotVuHWGmXkGUsY+SGKYNcFi3VIQd0dLPLo7PikWegCXlzdfHXi2Y7KQeBqH65kPqtDAsgdk9JlLqu9e3PfHxtSsvLNYDRiTLcIEVVyfrpkxMRcZalXpjS/9FFIQpizrbFoNuUCgYEA8TUjAVSrz5Fci+yfFwOW/HxOcIrDgUjEflQzolufUnFdAv3ILVyxd+BXDzyhZF9FzFPXsn8uCj2FR8P1TuOY3iM+V0Izs+DnucSSY/VW2FvXi9p/jwILuBTEC6PXQFbn+MydqBO3N/cPwWUgYWt9WNm/pvr11nfpbXYZp6y+uL8CgYEAyNk2TN2E86tSet/E0Dwlpfo+uK1oTg+mfsFJev9urmDmUXCW8xCYyazog+4xB83MNm8/V/ihtCyWyk2UgvbTBd3hfKGBJWrt+AAF1fM2y0hbkve20CLF7ZZeLVCufSVj+eagjcnoeZ5bs4lu49ACBWCDb2krUATY6xj2ilSftmMCgYEAvifX6uqncEH2pdDrMqd/1pjg9dRPgJKvZbBX7H0ywznfy8Xqk+hpeEoCGF8CFTEOw6CtgWjGEQijFAqmT4UaNLWwJfZo8Dw0Mr2HcwotZvAwo26j9Uf1mS+1xj9qKKzav8f/2kuAu7woTZy9xE+LSAqSDr/2IxfxjHv4ibmjud0CgYAa2whqsLgFSOQnb+JGIbS7A8H9OZqXzXnquveTTbJD+MrzGsXkTFTRqqAQe/nsXDUxFiD3J1Sf3dJvzH3OISjIQQnUe1fkY+7b2Uzuda3e8pbkCOFV5UBn61I+Zupd63D5yj+vlc7S7DzAveCo73hFtcj9Taev9GX40YatqnEMlwKBgQCeO+4N/7V3f46ju6yz+qpnH0VyAbe6RlVTqqS9IBIMNAuXL1yq8paGKrep8St2jOKfYJC9iOnZwIfWe0A9z4zz6Lslr7eeoj/oFCGRob7eOK1NrHFWxleom7DWZ0wnGHBMkvtwnyRy/8T8FJixAQJqeXWEvrWX1/sK24+A6o8V8w==");
//		aliPayConfig.setAliPayPublicKey("MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAvT4sjVgn0ELyhneO2Q5y9SJ9alrWZc9jG+9klO/MHWo18PB+dD7RAGsclwtWUMFfkvETcMlrvabNGX8rS7HORZH7rNDRaCixIEUdALDzIjvFQ6gG/4PCWicV40hLyu2JD88IvYJjkGa0mfumegKJ8eRhhbKxC4TLQgIzWhVC5sZkgwgSLfNF89lDAk80R6Ar1pOhtd234m+h2YQVwxCjJ0BDVL5lD+rsZkMoSX0JRZ6BA08L8EHqFuIGUk9Iad6kj9iAPzPqDghmPNLgeV7Q7E4MTnwGcmQX0FrigrqNCJFvJ5a1AdxYU90mb4uS8vjZvxmNKfsxJt9J+yCwz7o73QIDAQAB");
        aliPayConfig.setAliPayPublicKey("MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAkP122miaAWmv8kZISSckvzNXVcM/u6LG4L3ZWHuZNqGbHkhIqvEWHCqS2NVvUet++MksmXPWLpG+NvXLAjczXdsGuDHU3asuHNnTIM/Ah/DASxvf/GY7HlxylSnA0zY4eB3ETb1FDR0J6k7KCV4hl0QuepNDALnuhzqJeJPTM/cGzVvK0IXcOUyWl9mHLbcidHuJEloZBm23tQCDPxbriM6qM+omhTrHG2/RURhBm119RKbzt2QY6KJBMpDC3GXV4hPdFNFKzph1q3GOoEJWWt+KPMy4XlLVzu6+g6mDqELGmkqmoIpzjYU9VV1hqkv4PEMDXkaGnAQP4HsJgvAGswIDAQAB");
        aliPayConfig.setNotifyUrl("http://eu6c6c.natappfree.cc/pay/notify");
        aliPayConfig.setReturnUrl("http://eu6c6c.natappfree.cc");
//		是沙箱环境
        aliPayConfig.setSandbox(true);
        return aliPayConfig;
    }

    @Bean
    public WxPayConfig wxPayConfig(){
        //		微信支付配置
        WxPayConfig wxPayConfig = new WxPayConfig();

//		-appid
        wxPayConfig.setAppId("wx4648a3be714ccd3f");
//		-商户号
        wxPayConfig.setMchId("1548853501");
//		-商户秘钥
        wxPayConfig.setAppSecret("6307a227fae2d9a3e3b3179c05bc4686");
        wxPayConfig.setMchKey("souainettop2019hellnets2020anets");
//		-异步通知
        wxPayConfig.setNotifyUrl("http://eu6c6c.natappfree.cc/pay/notify");
        return wxPayConfig;
    }

}
