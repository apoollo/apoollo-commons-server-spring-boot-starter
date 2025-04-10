/**
 * 
 */
package com.apoollo.commons.server.spring.boot.starter.component.interceptor;

import java.time.Duration;

import org.apache.commons.lang3.BooleanUtils;
import org.springframework.web.servlet.ModelAndView;

import com.apoollo.commons.server.spring.boot.starter.model.Constants;
import com.apoollo.commons.server.spring.boot.starter.properties.InterceptorCommonsProperties;
import com.apoollo.commons.server.spring.boot.starter.service.AbstractInternalHandlerInterceptor;
import com.apoollo.commons.server.spring.boot.starter.service.FlowLimiter;
import com.apoollo.commons.server.spring.boot.starter.service.RequestResourceManager;
import com.apoollo.commons.server.spring.boot.starter.service.SyncService;
import com.apoollo.commons.util.exception.AppForbbidenException;
import com.apoollo.commons.util.exception.AppNoRequestResourceException;
import com.apoollo.commons.util.exception.AppServerOverloadedException;
import com.apoollo.commons.util.request.context.RequestContext;
import com.apoollo.commons.util.request.context.RequestResource;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * @author liuyulong
 * @since 2023年9月25日
 */
public class RequestResourceInterceptor extends AbstractInternalHandlerInterceptor {

	private static final String ASYNC_KEY = "request-resource";

	private RequestResourceManager requestResourceManager;
	private FlowLimiter flowLimiter;
	private SyncService syncService;

	public RequestResourceInterceptor(InterceptorCommonsProperties interceptorProperties,
			RequestResourceManager requestResourceManager, FlowLimiter flowLimiter, SyncService syncService) {
		super(interceptorProperties);
		this.requestResourceManager = requestResourceManager;
		this.flowLimiter = flowLimiter;
		this.syncService = syncService;
	}

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {

		RequestContext requestContext = RequestContext.getRequired();
		RequestResource requestResource = requestResourceManager
				.getRequestResource(requestContext.getRequestMappingPath());
		if (null == requestResource) {
			throw new AppNoRequestResourceException(
					"can't find requestResource - " + requestContext.getRequestMappingPath());
		}
		requestContext.setRequestResource(requestResource);
		if (BooleanUtils.isNotTrue(requestResource.getEnable())) {
			throw new AppForbbidenException("requestResource disabled - " + requestResource.getRequestMappingPath());
		}
		if (BooleanUtils.isTrue(requestResource.getEnableSync())) {
			if (!syncService.lock(ASYNC_KEY, Duration.ofSeconds(30))) {
				throw new AppServerOverloadedException("没有抢到同步锁");
			}
		}
		flowLimiter.tryAccess(null, requestResource.getResourcePin(), requestResource.getLimtPlatformQps());
		return true;
	}

	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
			ModelAndView modelAndView) throws Exception {
		RequestContext requestContext = RequestContext.getRequired();
		RequestResource requestResource = requestContext.getRequestResource();
		if (BooleanUtils.isTrue(requestResource.getEnableSync())) {
			syncService.unlock(ASYNC_KEY);
		}
	}

	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
			throws Exception {

	}

	@Override
	public int getOrder() {
		return Constants.REQUEST_RESOUCE_INTERCEPTOR_ORDER;
	}

}
