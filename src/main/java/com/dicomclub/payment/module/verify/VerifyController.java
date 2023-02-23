package com.dicomclub.payment.module.verify;

import com.alibaba.fastjson.JSON;
import com.dicomclub.payment.common.utils.*;
import com.dicomclub.payment.common.utils.HttpClientUtil;
import com.dicomclub.payment.common.utils.MD5Utils;
import com.dicomclub.payment.common.utils.RSAUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

/**
 * @author ftm
 * @date 2023/1/31 0031 17:32
 */
@Slf4j
@RestController
@Verify
@RequestMapping("verify")
public class VerifyController {
    @Verify(VerifyType.MD5withRSA)
    @RequestMapping("testMD5withRSA")
    public void test(@RequestBody List<Integer> map){
        System.out.println(map);
    }

    @Verify(VerifyType.MD5)
    @RequestMapping("testMD5")
    public void testMD5(String name,String data,String sign){
        System.out.println(name+data+sign);
    }


    @Verify(VerifyType.MD5withAES)
    @RequestMapping("testMD5withAES")
    public void MD5withAES(@RequestBody List<Integer> map){
        System.out.println(map);
    }


    public static void main(String[] args) throws Exception {

            testMD5WITHRSAorAES();
//        testMD5();
    }

    private static void testMD5() {
        String publicInv = "swiu382j22";
        String appSecret = "43322253sss";
        Map<String,Object>  request = new HashMap<>();
        request.put("timestamp", System.currentTimeMillis());
        request.put("appId", "123");
        request.put("appSecret", appSecret);
        request.put("name", "洛神沄");
        request.put("age", "18");
        request.put("sign", MD5Utils.MD5Lower(RSAUtil.sortAndGroupStringParam(request), appSecret+publicInv));
        request.put("data",Arrays.asList(1,2,3,4));
//        HttpClientUtil.doPost("http://127.0.0.1:2398/verify/testMD5", JSON.toJSONString(request), "utf-8");
        HttpClientUtil.doGet("http://127.0.0.1:2398/verify/testMD5",request, "utf-8");

    }

    private static void testMD5WITHRSAorAES() throws Exception {

//      我们持有私钥
        String privateKey =  "MIICdgIBADANBgkqhkiG9w0BAQEFAASCAmAwggJcAgEAAoGBANJSY5fJCZhhKdlpynRu28BkI68I0+3OLECnt4mbOYAbiaDL9WCtj+zUEMuH2pM0chFZd6lS3Ayt/WRi6X8DjmoT/ajEht56rjbfxgNZDsyh05VdH3K0QexPSOkIW+hh9hdhmeehZGssgHKVB5fOrFssPHEHEYKzgCOhEqHlLuc1AgMBAAECgYEAqTB9zWx7u4juEWd45ZEIVgw4aGXBllt0Xc6NZrTn3JZKcH+iNNNqJCm0GQaAXkqiODKwgBWXzttoK4kmLHa/6D7rXouWN8PGYXj7DHUNzyOe3IgmzYanowp/A8gu99mJQJzyhZGQ+Uo9dZXAgUDin6HAVLaxF3yWD8/yTKWN4UECQQD8Q72r7qdAfzdLMMSQl50VxRmbdhQYbo3D9FmwUw6W1gy2jhJyPXMi0JZKdKaqhxMZIT3zy4jYqw8/0zF2xc5/AkEA1W+n24Ef3ucbPgyiOu+XGwW0DNpJ9F8D3ZkEKPBgjOMojM7oqlehRwgy52hU+HaL4Toq9ghL1SwxBQPxSWCYSwJAGQUO9tKAvCDh9w8rL7wZ1GLsG0Mm0xWD8f92NcrHE6a/NAv7QGFf3gAaJ+BR92/WMRPe9SMmu3ab2JS1vzX3OQJAdN70/T8RYo8N3cYxNzBmf4d59ee5wzQb+8WD/57QX5UraR8LS+s8Bpc4uHnqvTq8kZG2YI5eZ9YQ6XwlLVbVTQJAKOSXNT+XEPWaol1YdWZDvr2m/ChbX2uwz52s8577Tey96O4Z6S/YA7V6Fr7hZEzkNF+K0LNUd79EOB6m2eQq5w==";
        String publicKey ="MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDSUmOXyQmYYSnZacp0btvAZCOvCNPtzixAp7eJmzmAG4mgy/VgrY/s1BDLh9qTNHIRWXepUtwMrf1kYul/A45qE/2oxIbeeq4238YDWQ7ModOVXR9ytEHsT0jpCFvoYfYXYZnnoWRrLIBylQeXzqxbLDxxBxGCs4AjoRKh5S7nNQIDAQAB";

        String aseKeyPublic = "ejdhsgdhtyiojhrr";
        String aseKeyIv = "zcits@loginIV161";
//      给调用方生成秘钥
        Map<String,Object> map = new HashMap<>();
        map.put("appId","hosCode");
        map.put("expireTime" ,new Date().getTime());
        map.put("appUrl", "http://www.baidu.com/");
        String appSecret = MD5Utils.MD5(RSAUtil.sortAndGroupStringParam(map)+ UUID.randomUUID());
        System.out.println("调用方秘钥:"+appSecret);
        map.put("appSecret", appSecret);
//      给调用方生成授权码
//        String authCode =  RSAUtil.encryptByPubKey(JSON.toJSONString(map),publicKey);
        String authCode =  RSAUtil.publicKeyEncrypt(JSON.toJSONString(map),publicKey,"xxx");
//        String authCode =  AESUtil.encryptAES(JSON.toJSONString(map), aseKeyPublic, aseKeyIv);
        System.out.println("调用方授权码:"+authCode);

        for(int i=0;i<100;i++){
            long start = System.currentTimeMillis();
//      调用方调用
            System.out.println("调用方开始调用");
            Map<String,Object>  request = new HashMap<>();
            request.put("timestamp", System.currentTimeMillis());
            request.put("authCode", authCode);
            request.put("sign", MD5Utils.MD5Lower(RSAUtil.sortAndGroupStringParam(request), appSecret));
            request.put("data", Arrays.asList(1,2,3));
            System.out.println("调用参数:"+request);
            long end = System.currentTimeMillis();
            System.out.println("加密耗时:"+(end-start));
            HttpClientUtil.doPost("http://127.0.0.1:2398/verify/testMD5withRSA", JSON.toJSONString(request), "utf-8");
            Thread.sleep(20000);
        }
    }


}
