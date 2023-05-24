package com.dicomclub.payment.module.pay.model.union;

import com.dicomclub.payment.module.pay.model.PayResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

/**
 * @author ftm
 * @date 2023/2/21 0021 13:50
 */
@AllArgsConstructor
@Builder
@Data
public class UnionPayResponse extends PayResponse {
    @Override
    public ResponseEntity buildNotifyedSuccessResponse() {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.TEXT_HTML);
        return new ResponseEntity("success", httpHeaders, HttpStatus.OK);
    }
}
