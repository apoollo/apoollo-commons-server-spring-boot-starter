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
	public static final int REQUEST_CONTENT_ESCAPE_FILTER_ORDER = REQUEST_CONTEXT_FILTER_ORDER + INCREMENT;

	// handlerExceptionResolver
	public static final int HANDLER_EXCEPTION_RESOVER = INITAIL;

	public static final String EXCEPTION_FORWARD_CONTROLLE_PATH = "/exception/forward";

	public static final String REQUEST_ATTRIBUTE_EXCEPTION = "commons-server-exception";

	public static final String CONFIGURATION_PREFIX = "apoollo.commons.server";
}
