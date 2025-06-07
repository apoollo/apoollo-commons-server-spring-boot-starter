/**
 * 
 */
package com.apoollo.commons.server.spring.boot.starter.service;

import com.apoollo.commons.util.request.context.RequestContext;
import com.apoollo.commons.util.request.context.limiter.support.LimitersSupport;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * liuyulong
 */
public interface SecurePrincipal<T extends LimitersSupport> {

	public T init(HttpServletRequest request, HttpServletResponse response, RequestContext requestContext);
}
