package com.dicomclub.payment.controller;

import com.alibaba.fastjson.JSONObject;
import com.dicomclub.payment.util.HttpRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * ClassName:InterfaceController <br/>
 * Function:  <br/>
 * Reason:	 TODO ADD REASON. <br/>
 * Date:     2023/2/6 14:49 <br/>
 *
 * @author sheng.chen
 * @see
 * @since JDK 1.8
 */

@RestController
@Slf4j
@RequestMapping("/us")
public class UsInterfaceController {

    @PostMapping("/sendData")
    public void sendData(@RequestBody JSONObject jsonObject) {
        log.info("请求json为："+jsonObject);
        String result = HttpRequest.doPost("http://localhost:6001/registrationcheck/registerInfo/uploadStudyInfo", jsonObject.toJSONString(), "UTF-8");
        log.info("result="+result);
    }
}
