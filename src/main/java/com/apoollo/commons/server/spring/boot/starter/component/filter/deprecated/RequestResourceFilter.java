/**
 * 
 */
package com.apoollo.commons.server.spring.boot.starter.component.filter.deprecated;

import java.io.IOException;

import org.apache.commons.lang3.BooleanUtils;

import com.apoollo.commons.server.spring.boot.starter.component.filter.AbstractSecureFilter;
import com.apoollo.commons.server.spring.boot.starter.properties.PathProperties;
import com.apoollo.commons.server.spring.boot.starter.service.RequestResourceManager;
import com.apoollo.commons.util.exception.AppForbbidenException;
import com.apoollo.commons.util.exception.AppNoRequestResourceException;
import com.apoollo.commons.util.request.context.RequestContext;
import com.apoollo.commons.util.request.context.RequestResource;
import com.apoollo.commons.util.request.context.limiter.Limiters;
import com.apoollo.commons.util.request.context.limiter.support.LimitersSupport;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * @author liuyulong
 * @since 2025-05-19
 */
public class RequestResourceFilter extends AbstractSecureFilter {

	//private static final Logger LOGGER = LoggerFactory.getLogger(RequestResourceFilter.class);


	private RequestResourceManager requestResourceManager;
	private Limiters<LimitersSupport> limiters;

	public RequestResourceFilter(PathProperties pathProperties, RequestResourceManager requestResourceManager,
			Limiters<LimitersSupport> limiters) {
		super(pathProperties);
		this.requestResourceManager = requestResourceManager;
		this.limiters = limiters;
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
		limiters.limit(request, response, requestContext, requestResource);
		/*
		 * if (BooleanUtils.isTrue(requestResource.getEnableSyncLimiter())) {
		 * syncLimiter.limit(ASYNC_KEY, Duration.ofSeconds(30)); }
		 * flowLimiter.limit(null, requestResource.getResourcePin(),
		 * requestResource.getFlowLimiterLimitCount());
		 * 
		 * LOGGER.info("request resource accessed");
		 */
		//
		chain.doFilter(request, response);

		/*
		 * // if (BooleanUtils.isTrue(requestResource.getEnableSyncLimiter())) {
		 * syncLimiter.unlimit(ASYNC_KEY); }
		 */

	}

}
