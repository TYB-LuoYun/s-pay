package com.dicomclub.payment.module.pay.model.union;

import com.dicomclub.payment.module.pay.model.PayResponse;
import lombok.Data;
import org.springframework.http.ResponseEntity;

/**
 * @author ftm
 * @date 2023/2/21 0021 13:50
 */
@Data
public class UnionPayResponse extends PayResponse {
    @Override
    public ResponseEntity buildPaySuccessResponse() {
        return null;
    }
}
