/**
 * 
 */
package com.apoollo.commons.server.spring.boot.starter.component.filter;

import java.io.IOException;
import java.time.Duration;

import org.apache.commons.lang3.BooleanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.apoollo.commons.server.spring.boot.starter.properties.PathProperties;
import com.apoollo.commons.server.spring.boot.starter.service.RequestResourceManager;
import com.apoollo.commons.util.exception.AppForbbidenException;
import com.apoollo.commons.util.exception.AppNoRequestResourceException;
import com.apoollo.commons.util.request.context.RequestContext;
import com.apoollo.commons.util.request.context.RequestResource;
import com.apoollo.commons.util.request.context.limiter.FlowLimiter;
import com.apoollo.commons.util.request.context.limiter.SyncLimiter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * @author liuyulong
 * @since 2025-05-19
 */
public class RequestResourceFilter extends AbstractSecureFilter {

	private static final Logger LOGGER = LoggerFactory.getLogger(RequestResourceFilter.class);

	private static final String ASYNC_KEY = "request-resource";

	private RequestResourceManager requestResourceManager;
	private FlowLimiter flowLimiter;
	private SyncLimiter syncService;

	public RequestResourceFilter(PathProperties pathProperties, RequestResourceManager requestResourceManager,
			FlowLimiter flowLimiter, SyncLimiter syncService) {
		super(pathProperties);
		this.requestResourceManager = requestResourceManager;
		this.flowLimiter = flowLimiter;
		this.syncService = syncService;
	}

	@Override
	public void doSecureFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws IOException, ServletException {
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
			syncService.limit(ASYNC_KEY, Duration.ofSeconds(30));
		}
		flowLimiter.limit(null, requestResource.getResourcePin(), requestResource.getLimtPlatformQps());

		LOGGER.info("request resource accessed");
		//
		chain.doFilter(request, response);

		//
		if (BooleanUtils.isTrue(requestResource.getEnableSync())) {
			syncService.unlimit(ASYNC_KEY);
		}

	}

}
