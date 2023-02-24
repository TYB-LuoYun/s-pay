package com.dicomclub.payment.module.pay.model.alipay;

import com.dicomclub.payment.module.pay.enums.PayDataType;
import com.dicomclub.payment.module.pay.model.PayResponse;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

/**
 * @author ftm
 * @date 2023/2/17 0017 14:49
 */
@Data
public class AliPayResponse extends PayResponse {


    @Override
    public ResponseEntity buildPaySuccessResponse() {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.TEXT_HTML);
        return new ResponseEntity("success", httpHeaders, HttpStatus.OK);
    }
}
