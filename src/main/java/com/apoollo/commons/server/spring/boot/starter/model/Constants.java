/**
 * 
 */
package com.apoollo.commons.server.spring.boot.starter.model;

/**
 * @author liuyulong
 * @since 2023年8月23日
 */
public interface Constants {

	// rulers
	public static final int INITAIL = 100;
	public static final int INCREMENT = 10;

	// filters
	public static final int XSS_FILTER_ORDER = INITAIL;

	// interceptors
	public static final int REQUEST_CONTEXT_INTERCEPTOR_ORDER = INITAIL;
	public static final int REQUEST_RESOUCE_INTERCEPTOR_ORDER = REQUEST_CONTEXT_INTERCEPTOR_ORDER + INCREMENT;

	public static final int JWT_TOKEN_ACCESS_INTERCEPTOR_ORDER = REQUEST_RESOUCE_INTERCEPTOR_ORDER + INCREMENT;
	public static final int SECRET_KEY_TOKEN_ACCESS_INTERCEPTOR_ORDER = JWT_TOKEN_ACCESS_INTERCEPTOR_ORDER + INCREMENT;

	// requestBodyAdvices
	public static final int REQUEST_KEEP_PARAMETER_BODY_ADVICE_ORDER = INITAIL;
	public static final int REQUEST_DIGEST_VALIDATE_BODY_ADVICE_ORDER = REQUEST_KEEP_PARAMETER_BODY_ADVICE_ORDER
			+ INCREMENT;

	public static final int REQUEST_JWT_TOKEN_ACCESS_BODY_ADVICE_ORDER = REQUEST_DIGEST_VALIDATE_BODY_ADVICE_ORDER
			+ INCREMENT;
	public static final int REQUEST_SECRET_KEY_TOKEN_ACCESS_BODY_ADVICE_ORDER = REQUEST_JWT_TOKEN_ACCESS_BODY_ADVICE_ORDER
			+ INCREMENT;

	public static final String HEADER_NEED_RESET_PASSWORD = "NeedResetPassword";

	public static final String CONFIGURATION_PREFIX = "hisign.commons.server";
}
