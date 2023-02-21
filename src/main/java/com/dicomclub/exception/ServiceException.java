/**
 * 
 */
package com.dicomclub.exception;

/**
 * @author Administrator
 *
 */
public class ServiceException extends RuntimeException{
    private String code ;
    private String message ;
    
	public ServiceException() {
		super();
	}
	
	public ServiceException(String message,Throwable cause) {
		super(message, cause);
		this.message=message;
	}
	
	public ServiceException(String message) {
		super(message);
		this.message=message;
	}
	
	
	public ServiceException(String code ,String message) {
		super(message);
		this.code=code;
		this.message=message;
	}
	
	public ServiceException(Throwable cause) {
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
