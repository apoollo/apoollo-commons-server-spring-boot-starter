/**
 * 
 */
package com.apoollo.commons.server.spring.boot.starter.component.interceptor;

import java.util.concurrent.ThreadPoolExecutor;

import org.apache.commons.lang3.BooleanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.ModelAndView;

import com.apoollo.commons.server.spring.boot.starter.compatible.CompatibleUtils;
import com.apoollo.commons.server.spring.boot.starter.model.Constants;
import com.apoollo.commons.server.spring.boot.starter.properties.InterceptorCommonsProperties;
import com.apoollo.commons.server.spring.boot.starter.service.AbstractInternalHandlerInterceptor;
import com.apoollo.commons.server.spring.boot.starter.service.LoggerWriter;
import com.apoollo.commons.util.IpUtils;
import com.apoollo.commons.util.LangUtils;
import com.apoollo.commons.util.redis.service.CountLimiter;
import com.apoollo.commons.util.request.context.RequestContext;
import com.apoollo.commons.util.request.context.RequestContextInitail;
import com.apoollo.commons.util.request.context.User;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * @author liuyulong
 */

public class RequestContextInterceptor extends AbstractInternalHandlerInterceptor {

	private static final Logger LOGGER = LoggerFactory.getLogger(RequestContextInterceptor.class);

	private RequestContextInitail requestContextInitail;
	private ThreadPoolExecutor threadPoolExecutor;
	private CountLimiter countLimiter;
	private LoggerWriter logWitter;

	public RequestContextInterceptor(InterceptorCommonsProperties interceptorProperties,
			RequestContextInitail requestContextInitail, ThreadPoolExecutor threadPoolExecutor,
			CountLimiter countLimiter, LoggerWriter logWitter) {
		super(interceptorProperties);
		this.requestContextInitail = requestContextInitail;
		this.threadPoolExecutor = threadPoolExecutor;
		this.countLimiter = countLimiter;
		this.logWitter = logWitter;
	}

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {

		String requestId = LangUtils.defaultString(request.getHeader("request-id"), () -> LangUtils.getUppercaseUUID());
		String reuqestUri = request.getRequestURI();
		String requestIp = IpUtils.tryGetRealIp(request);
		RequestContext requestContext = RequestContext.reset(requestId, request.getContextPath(), reuqestUri,
				requestContextInitail::newInstance);
		requestContext.setRequestIp(requestIp);
		requestContext.setRequestServerName(request.getServerName());
		requestContext.setThreadPoolExecutor(threadPoolExecutor);
		CompatibleUtils.compatibleResponse(response, requestId);
		LOGGER.info("请求进入标记");
		LOGGER.info("访问URI：" + reuqestUri);
		LOGGER.info("访问IP：" + requestIp);
		return true;
	}

	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
			ModelAndView modelAndView) throws Exception {
		RequestContext requestContext = RequestContext.getRequired();
		User user = requestContext.getUser();
		if (null != user) {
			response.setHeader(Constants.HEADER_NEED_RESET_PASSWORD, String.valueOf(user.needResetPassword()));
		}
	}

	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
			throws Exception {
		try {
			RequestContext requestContext = RequestContext.getRequired();
			if (null != requestContext.getDailyMaximumUseTimesLimitKey()
					&& BooleanUtils.isNotTrue(requestContext.getResponseIsChargeForUseTimesLimit())) {
				countLimiter.decrement(requestContext.getDailyMaximumUseTimesLimitKey());
			}
			logWitter.write(requestContext, () -> {
				LOGGER.info("请求结束标记");
			});
		} finally {
			RequestContext.release();
		}

	}

	@Override
	public int getOrder() {
		return Constants.REQUEST_CONTEXT_INTERCEPTOR_ORDER;
	}

}
