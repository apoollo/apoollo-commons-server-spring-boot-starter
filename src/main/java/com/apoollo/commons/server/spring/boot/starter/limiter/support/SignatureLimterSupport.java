/**
 * 
 */
package com.apoollo.commons.server.spring.boot.starter.limiter.support;

import java.util.List;

/**
 * @author liuyulong
 * @since 2025-06-05
 */
public interface SignatureLimterSupport {

	Boolean getEnableSignatureLimiter();

	String getSignatureLimiterSecret();

	List<String> getSignatureLimiterExcludeHeaderNames();

	List<String> getSignatureLimiterIncludeHeaderNames();
}
