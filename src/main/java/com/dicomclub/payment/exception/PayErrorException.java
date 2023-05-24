package com.dicomclub.payment.exception;

/**
 * @author ftm
 * @date 2023/3/3 0003 13:21
 */
public class PayErrorException extends RuntimeException  {

    private PayError error;

    public PayErrorException(PayError error) {
        super(error.getString());
        this.error = error;
    }

    public PayErrorException(PayError error, Throwable throwable) {
        super(error.getString(), throwable);
        this.error = error;
    }


    public PayError getPayError() {
        return error;
    }
}
