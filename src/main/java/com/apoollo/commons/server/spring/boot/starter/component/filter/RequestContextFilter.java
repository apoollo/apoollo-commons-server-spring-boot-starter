/**
 * 
 */
package com.apoollo.commons.server.spring.boot.starter.component.filter;

import java.io.IOException;

import org.apache.commons.lang3.BooleanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.apoollo.commons.server.spring.boot.starter.model.Constants;
import com.apoollo.commons.server.spring.boot.starter.model.Version;
import com.apoollo.commons.server.spring.boot.starter.properties.PathProperties;
import com.apoollo.commons.server.spring.boot.starter.service.LoggerWriter;
import com.apoollo.commons.util.IpUtils;
import com.apoollo.commons.util.LangUtils;
import com.apoollo.commons.util.redis.service.CountLimiter;
import com.apoollo.commons.util.request.context.RequestContext;
import com.apoollo.commons.util.request.context.RequestContextInitail;
import com.apoollo.commons.util.request.context.User;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * @author liuyulong
 * @since 2025-05-16
 */
public class RequestContextFilter extends AbstractSecureFilter {

	private static final Logger LOGGER = LoggerFactory.getLogger(RequestContextFilter.class);

	private RequestContextInitail requestContextInitail;
	private CountLimiter countLimiter;
	private LoggerWriter logWitter;

	public RequestContextFilter(PathProperties pathProperties, RequestContextInitail requestContextInitail,
			CountLimiter countLimiter, LoggerWriter logWitter) {
		super(pathProperties);
		this.requestContextInitail = requestContextInitail;
		this.countLimiter = countLimiter;
		this.logWitter = logWitter;
	}

	@Override
	public void doSecureFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		LOGGER.info("请求进入标记");
		String clientRequestId = request.getHeader(Constants.REQUEST_HEADER_REQUEST_ID);

		String requestId = LangUtils.getUppercaseUUID();
		String reuqestUri = request.getRequestURI();
		String requestIp = IpUtils.tryGetRealIp(request);
		RequestContext requestContext = RequestContext.reset(requestId, request.getContextPath(), reuqestUri,
				requestContextInitail::newInstance);
		requestContext.setClientRequestId(clientRequestId);
		requestContext.setRequestIp(requestIp);
		requestContext.setRequestServerName(request.getServerName());
		if (null != clientRequestId) {
			LOGGER.info("client-request-id：" + clientRequestId);
		}
		LOGGER.info("访问URI：" + reuqestUri);
		LOGGER.info("访问IP：" + requestIp);

		//
		chain.doFilter(request, response);
		//
		response.setHeader(Constants.RESPONSE_HEADER_VERSION, Version.CURRENT_VERSION);
		User user = requestContext.getUser();
		if (null != user) {
			response.setHeader(Constants.RESPONSE_HEADER_NEED_RESET_PASSWORD, String.valueOf(user.needResetPassword()));
		}
		if (null != requestContext.getDailyMaximumUseTimesLimitKey()
				&& BooleanUtils.isNotTrue(requestContext.getResponseIsChargeForUseTimesLimit())) {
			countLimiter.decrement(requestContext.getDailyMaximumUseTimesLimitKey());
		}
		logWitter.write(requestContext, () -> {
			LOGGER.info("请求结束标记");
		});
	}

	@Override
	public void cleanupMatches(HttpServletRequest request, HttpServletResponse response, FilterChain chain) {
		super.cleanAttribute(request);
		RequestContext.release();
	}

	@Override
	public void cleanupNoMaches(HttpServletRequest request, HttpServletResponse response, FilterChain chain) {
		super.cleanAttribute(request);
	}
}
