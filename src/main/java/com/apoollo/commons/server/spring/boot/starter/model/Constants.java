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
	public static final int REQUEST_CONTEXT_FILTER_ORDER = INITAIL;
	public static final int REQUEST_RESOURCE_FILTER_ORDER = REQUEST_CONTEXT_FILTER_ORDER + INCREMENT;
	public static final int REQUEST_NONCE_VALIDATE_FILTER_ORDER = REQUEST_RESOURCE_FILTER_ORDER + INCREMENT;
	public static final int REQUEST_SIGNATURE_VALIDATE_FILTER_ORDER = REQUEST_NONCE_VALIDATE_FILTER_ORDER + INCREMENT;
	public static final int REQUEST_CONTENT_ESCAPE_FILTER_ORDER = REQUEST_SIGNATURE_VALIDATE_FILTER_ORDER + INCREMENT;
	public static final int REQUEST_HEADER_JWT_TOKEN_ACCESS_FILTER_ORDER = REQUEST_CONTENT_ESCAPE_FILTER_ORDER
			+ INCREMENT;
	public static final int REQUEST_HEADER_KEY_PAIR_ACCESS_FILTER_ORDER = REQUEST_HEADER_JWT_TOKEN_ACCESS_FILTER_ORDER
			+ INCREMENT;
	public static final int REQUEST_PARAMETER_KEY_PAIR_ACCESS_FILTER_ORDER = REQUEST_HEADER_KEY_PAIR_ACCESS_FILTER_ORDER
			+ INCREMENT;

	// requestBodyAdvices
	public static final int REQUEST_BODY_JWT_TOKEN_ACCESS_ADVICE_ORDER = INITAIL;

	public static final int REQUEST_BODY_KEY_PAIR_ACCESS_ADVICE_ORDER = REQUEST_BODY_JWT_TOKEN_ACCESS_ADVICE_ORDER
			+ INCREMENT;

	public static final String EXCEPTION_FORWARD_CONTROLLE_PATH = "/exception/forward";

	public static final String REQUEST_ATTRIBUTE_EXCEPTION = "commons-server-exception";

	public static final String REQUEST_HEADER_REQUEST_ID = "request-id";

	public static final String REQUEST_HEADER_TIMESTAMP = "x-timestamp";

	public static final String REQUEST_HEADER_NONCE = "x-nonce";

	public static final String REQUEST_HEADER_SIGNATURE = "x-signature";

	public static final String RESPONSE_HEADER_VERSION = "x-version";

	public static final String RESPONSE_HEADER_NEED_RESET_PASSWORD = "x-need-reset-password";

	public static final String RESPONSE_HEADER_RENEWAL_AUTHORIZATION = "x-renewal-authorization";

	public static final String CONFIGURATION_PREFIX = "apoollo.commons.server";
}
