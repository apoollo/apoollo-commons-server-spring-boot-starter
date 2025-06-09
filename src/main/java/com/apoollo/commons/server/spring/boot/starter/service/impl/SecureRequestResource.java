/**
 * 
 */
package com.apoollo.commons.server.spring.boot.starter.service.impl;

import org.apache.commons.lang3.BooleanUtils;

import com.apoollo.commons.server.spring.boot.starter.service.RequestResourceManager;
import com.apoollo.commons.server.spring.boot.starter.service.SecurePrincipal;
import com.apoollo.commons.util.exception.AppForbbidenException;
import com.apoollo.commons.util.exception.AppNoRequestResourceException;
import com.apoollo.commons.util.request.context.RequestContext;
import com.apoollo.commons.util.request.context.access.RequestResource;
import com.apoollo.commons.util.request.context.limiter.Limiters;
import com.apoollo.commons.util.request.context.limiter.support.LimitersSupport;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * liuyulong
 */
public class SecureRequestResource implements SecurePrincipal<RequestResource> {

	private RequestResourceManager requestResourceManager;
	private Limiters<LimitersSupport> limiters;

	public SecureRequestResource(RequestResourceManager requestResourceManager, Limiters<LimitersSupport> limiters) {
		super();
		this.requestResourceManager = requestResourceManager;
		this.limiters = limiters;
	}

	@Override
	public RequestResource init(HttpServletRequest request, HttpServletResponse response,
			RequestContext requestContext) {
		String requestMappingPath = requestContext.getRequestMappingPath();
		RequestResource requestResource = requestResourceManager.getRequestResource(requestMappingPath);
		if (null == requestResource) {
			throw new AppNoRequestResourceException("can't find requestResource - " + requestMappingPath);
		}
		requestContext.setRequestResource(requestResource);
		if (BooleanUtils.isNotTrue(requestResource.getEnable())) {
			throw new AppForbbidenException("requestResource disabled - " + requestMappingPath);
		}
		if (BooleanUtils.isNotFalse(requestResource.getEnableCapacity())) {
			limiters.limit(request, response, requestContext, requestResource);
		}
		return requestResource;
	}

}
