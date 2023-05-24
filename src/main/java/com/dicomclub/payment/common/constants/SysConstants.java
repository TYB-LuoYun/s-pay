package com.dicomclub.payment.common.constants;

/**
 * 系统常量类
 * @author Young
 * @date 2022/4/29
 */
public final class SysConstants {

    public static final String TOKEN_SERVICE = "token-service";
    public static final String REDIS_USER_PREFIX = "user-cache-id-";
    public static final String TOKEN_HEADER = "token";

    private SysConstants() {
        throw new AssertionError("No instance for you!");
    }


    /**
     * application/json;charset=utf-8
     */
    public static final String APPLICATION_JSON_UTF8 = "application/json;charset=utf-8";




    /**
     * redis 脚本
     */
    public static final String REDIS_SCRIPT = "if redis.call('EXISTS', KEYS[1]) == 1 then " +
            " redis.call('EXPIRE', KEYS[1], ARGV[2]) " +
            " return redis.call('INCR', KEYS[1]) " +
            " else " +
            " redis.call('SET', KEYS[1], ARGV[1], 'EX', ARGV[2]) " +
            " return 1 end";



    /**
     * 一分钟的秒数
     */
    public static final int SECONDS_OF_MINUTE = 60;

    /**
     * 密码正则
     */
    public static final String PWD_REG = "^[a-zA-Z0-9_!@#$%^&*(){}\\\\[\\\\]`~.,?<>/\\\\\\\\+=|;:'\\\"-]{8,20}$";
}
