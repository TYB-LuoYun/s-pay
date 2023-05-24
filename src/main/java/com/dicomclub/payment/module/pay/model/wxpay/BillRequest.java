package com.dicomclub.payment.module.pay.model.wxpay;
import com.dicomclub.payment.module.pay.enums.BillDataType;
import com.dicomclub.payment.module.pay.model.common.BillType;
import lombok.Data;

import java.util.Date;

/**
 * @author ftm
 * @date 2023/4/24 0024 12:01
 */
@Data
public class BillRequest {
    Date billDate;
    BillType billType;
    BillDataType billDataType = BillDataType.DOWN_URL;
}
