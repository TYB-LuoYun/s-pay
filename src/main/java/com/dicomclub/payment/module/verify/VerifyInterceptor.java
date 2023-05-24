package com.dicomclub.payment.module.verify;
import com.alibaba.fastjson.JSON;
import com.dicomclub.payment.common.utils.AESUtil;
import com.dicomclub.payment.common.utils.MD5Utils;
import com.dicomclub.payment.common.utils.RSAUtil;
import com.dicomclub.payment.common.utils.ServletUtil;
import com.dicomclub.payment.log.MyRequestWrapper;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * @author ftm
 *
 * 过滤器->拦截器->参数解析器->控制方法->AOP
 */
public class VerifyInterceptor implements HandlerInterceptor {
//  RSA密钥对
    private static final String publicKey ="MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDSUmOXyQmYYSnZacp0btvAZCOvCNPtzixAp7eJmzmAG4mgy/VgrY/s1BDLh9qTNHIRWXepUtwMrf1kYul/A45qE/2oxIbeeq4238YDWQ7ModOVXR9ytEHsT0jpCFvoYfYXYZnnoWRrLIBylQeXzqxbLDxxBxGCs4AjoRKh5S7nNQIDAQAB";
    private static final String privateKey =  "MIICdgIBADANBgkqhkiG9w0BAQEFAASCAmAwggJcAgEAAoGBANJSY5fJCZhhKdlpynRu28BkI68I0+3OLECnt4mbOYAbiaDL9WCtj+zUEMuH2pM0chFZd6lS3Ayt/WRi6X8DjmoT/ajEht56rjbfxgNZDsyh05VdH3K0QexPSOkIW+hh9hdhmeehZGssgHKVB5fOrFssPHEHEYKzgCOhEqHlLuc1AgMBAAECgYEAqTB9zWx7u4juEWd45ZEIVgw4aGXBllt0Xc6NZrTn3JZKcH+iNNNqJCm0GQaAXkqiODKwgBWXzttoK4kmLHa/6D7rXouWN8PGYXj7DHUNzyOe3IgmzYanowp/A8gu99mJQJzyhZGQ+Uo9dZXAgUDin6HAVLaxF3yWD8/yTKWN4UECQQD8Q72r7qdAfzdLMMSQl50VxRmbdhQYbo3D9FmwUw6W1gy2jhJyPXMi0JZKdKaqhxMZIT3zy4jYqw8/0zF2xc5/AkEA1W+n24Ef3ucbPgyiOu+XGwW0DNpJ9F8D3ZkEKPBgjOMojM7oqlehRwgy52hU+HaL4Toq9ghL1SwxBQPxSWCYSwJAGQUO9tKAvCDh9w8rL7wZ1GLsG0Mm0xWD8f92NcrHE6a/NAv7QGFf3gAaJ+BR92/WMRPe9SMmu3ab2JS1vzX3OQJAdN70/T8RYo8N3cYxNzBmf4d59ee5wzQb+8WD/57QX5UraR8LS+s8Bpc4uHnqvTq8kZG2YI5eZ9YQ6XwlLVbVTQJAKOSXNT+XEPWaol1YdWZDvr2m/ChbX2uwz52s8577Tey96O4Z6S/YA7V6Fr7hZEzkNF+K0LNUd79EOB6m2eQq5w==";
//  aes向量
    private static final String aseKeyPublic = "ejdhsgdhtyiojhrr";
    private static final String aseKeyIv = "zcits@loginIV161";
//  md5盐向量
    private static final String  publicInv = "swiu382j22";

    @Override
        public boolean preHandle(HttpServletRequest requestOri, HttpServletResponse response, Object handler) throws Exception {
        MyRequestWrapper request = (MyRequestWrapper) requestOri;
        HandlerMethod handlerMethod = (HandlerMethod) handler;
        Method method = handlerMethod.getMethod();
        long start = System.currentTimeMillis();
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        Verify annotation = method.getAnnotation(Verify.class);
        if(annotation == null){
            annotation = ((HandlerMethod) handler).getBean().getClass().getAnnotation(Verify.class);
        }
        if(annotation == null){
            return true;
        }
        String value = annotation.value();
        Map<String, Object> invoke = ServletUtil.getParams(request);
        System.out.println("对方调用参数:"+invoke);
//          验证时间戳是否过期
        Long time = Long.valueOf(  ""+invoke.get("timestamp"));
        long timeSpace = Math.abs(System.currentTimeMillis() - time);
        if (timeSpace > 5 * 60 * 1000) {
            throw new RuntimeException("时间校验不合法或者超过5分钟，请检查是否传输正确,当前时间:"+System.currentTimeMillis());
        }
        String sign = (String) invoke.get("sign");
        Object data = invoke.get("data");
        invoke.remove("sign");
        invoke.remove("data");
        String signNow = null;
        if(VerifyType.MD5withRSA.equals(value)||VerifyType.MD5withAES.equals(value)){
            Map<String, Object> map = parseSecurityCode( (String) invoke.get("authCode"),value);
            if(map.get("expireTime")!=null){
                Long expireTime = Long.valueOf(  ""+map.get("expireTime"));
                if(expireTime - System.currentTimeMillis() < 0){
                    throw new RuntimeException("授权过期");
                }
            }
            signNow = MD5Utils.MD5Lower(RSAUtil.sortAndGroupStringParam(invoke), (String) map.get("appSecret"));

        }else  if(VerifyType.MD5.equals(value)){
            signNow = MD5Utils.MD5Lower(RSAUtil.sortAndGroupStringParam(invoke), invoke.get("appSecret") + publicInv);
        }else{
            throw new RuntimeException("不支持的验签方式");
        }
//          验签
        if(!signNow.equals(sign)){
            throw new RuntimeException("验签失败");
        }
        long end = System.currentTimeMillis();
        System.out.println("验签耗时:"+(end-start));


        request.setBody(JSON.toJSONString(data));
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        //方法处理之后但是并未渲染视图的时候进行的操作
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        //渲染视图之后进行的操作
    }

    private static Map<String,Object> parseSecurityCode(String securityCode,String mode) throws Exception {

        //        String parse = RSAUtil.decryptByPriKey(securityCode,privateKey );
        String parse = null;
        if(VerifyType.MD5withRSA.equals(mode)){
            parse = RSAUtil.privateKeyDecrypt(securityCode,privateKey,"xxx" );
        }
        if(VerifyType.MD5withAES.equals(mode)){
            parse =  AESUtil.decryptAES(securityCode, aseKeyPublic, aseKeyIv);
        }
        return JSON.parseObject(parse, Map.class);
    }
}