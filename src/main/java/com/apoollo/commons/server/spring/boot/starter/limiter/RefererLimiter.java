/**
 * 
 */
package com.apoollo.commons.server.spring.boot.starter.limiter;

import com.apoollo.commons.server.spring.boot.starter.limiter.support.RefererLimiterSupport;

import jakarta.servlet.http.HttpServletRequest;

/**
 * @author liuyulong
 * @since 2025-06-05
 */
public interface RefererLimiter {

	public void limit(HttpServletRequest request, RefererLimiterSupport refererLimiterSupport);
}
