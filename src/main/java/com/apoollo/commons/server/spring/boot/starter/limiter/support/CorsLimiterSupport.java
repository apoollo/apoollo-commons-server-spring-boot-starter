/**
 * 
 */
package com.apoollo.commons.server.spring.boot.starter.limiter.support;

import org.springframework.web.cors.CorsConfiguration;

/**
 * @author liuyulong
 * @since 2025-06-05
 */
public interface CorsLimiterSupport {

	Boolean getEnableCorsLimiter();

	CorsConfiguration getCorsLimiterConfiguration();

}
