/**
 * 
 */
package com.apoollo.commons.server.spring.boot.starter.limiter;

import com.apoollo.commons.util.request.context.RequestContext;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * @author liuyulong
 * @since 2025-06-05
 */
public interface Limiters<T> {

	public void limit(HttpServletRequest request, HttpServletResponse response, RequestContext requestContext,
			T support);

	public void unlimit(HttpServletRequest request, HttpServletResponse response, RequestContext requestContext,
			T support);

}
