package com.dicomclub.payment.util;

import java.math.BigDecimal;

/**
 * 2017-07-02 13:53
 */
public class MoneyUtil {

    public static final BigDecimal HUNDRED = new BigDecimal(100);

    /**
     * 元转分
     * @param yuan
     * @return
     */
    public static Integer Yuan2Fen(Double yuan) {
        return BigDecimal.valueOf(yuan).movePointRight(2).intValue();
    }

    /**
     * 分转元
     * @param fen
     * @return
     */
    public static Double Fen2Yuan(Integer fen) {
        return new BigDecimal(fen).movePointLeft(2).doubleValue();
    }


    public static Integer Yuan2Fen(BigDecimal amount) {
        return amount.multiply(HUNDRED).setScale(0, BigDecimal.ROUND_HALF_UP).intValue();
    }
}
