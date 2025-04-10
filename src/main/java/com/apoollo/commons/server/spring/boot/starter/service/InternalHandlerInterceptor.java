/**
 * 
 */
package com.apoollo.commons.server.spring.boot.starter.service;

import org.springframework.core.Ordered;
import org.springframework.web.servlet.HandlerInterceptor;

import com.apoollo.commons.server.spring.boot.starter.properties.InterceptorCommonsProperties;

/**
 * @author liuyulong
 * @since 2023年9月1日
 */
public interface InternalHandlerInterceptor extends HandlerInterceptor, Ordered {

	public default InterceptorCommonsProperties getInterceptorCommonsProperties() {
		return null;
	}
}
