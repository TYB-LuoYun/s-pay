package com.dicomclub.payment.module.pay.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author ftm
 * @date 2023/4/24 0024 13:44
 */
@Getter
@AllArgsConstructor
public enum BillDataType   {
    DOWN_URL("下载链接"),
    FILE_STEAM("文件流");

    private String desc;
}
