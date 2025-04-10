/**
 * 
 */
package com.apoollo.commons.server.spring.boot.starter.service;

import com.apoollo.commons.server.spring.boot.starter.properties.InterceptorCommonsProperties;

/**
 * @author liuyulong
 * @since 2023年9月28日
 */
public abstract class AbstractInternalHandlerInterceptor implements InternalHandlerInterceptor {

	private InterceptorCommonsProperties interceptorProperties;

	public AbstractInternalHandlerInterceptor(InterceptorCommonsProperties interceptorProperties) {
		super();
		this.interceptorProperties = interceptorProperties;
	}

	@Override
	public InterceptorCommonsProperties getInterceptorCommonsProperties() {
		return this.interceptorProperties;
	}


}
