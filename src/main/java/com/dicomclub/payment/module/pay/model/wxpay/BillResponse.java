package com.dicomclub.payment.module.pay.model.wxpay;

import com.dicomclub.payment.module.pay.enums.BillDataType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.io.InputStream;

/**
 * @author ftm
 * @date 2023/4/24 0024 13:50
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BillResponse {
    BillDataType billDataType = BillDataType.DOWN_URL;
    private String downloadUrl;
    private InputStream inputStream;



    public Object buildData(){
        if(StringUtils.isNotEmpty(downloadUrl)){
            return downloadUrl;
        }
        return inputStream;
    }

}
