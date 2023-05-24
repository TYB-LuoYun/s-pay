package com.dicomclub.payment.module.pay.model.alipay;

import com.dicomclub.payment.module.pay.enums.PayDataType;
import com.dicomclub.payment.module.pay.model.PayResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
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
@AllArgsConstructor
@Builder
@Accessors
public class AliPayResponse extends PayResponse {

    @Override
    public ResponseEntity buildNotifyedSuccessResponse() {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.TEXT_HTML);
        return new ResponseEntity("success", httpHeaders, HttpStatus.OK);
    }
}
