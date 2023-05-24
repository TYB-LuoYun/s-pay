/**
 * 
 */
package com.dicomclub.payment.exception;

/**
 * @author Administrator
 *
 */
public class PayException extends RuntimeException{
    private String code ;
    private String message ;

	public PayException() {
		super();
	}

	public PayException(String message, Throwable cause) {
		super(message, cause);
		this.message=message;
	}

	public PayException(String message) {
		super(message);
		this.message=message;
	}


	public PayException(String code , String message) {
		super(message);
		this.code=code;
		this.message=message;
	}

	public PayException(Throwable cause) {
		super(cause);
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
	
}
