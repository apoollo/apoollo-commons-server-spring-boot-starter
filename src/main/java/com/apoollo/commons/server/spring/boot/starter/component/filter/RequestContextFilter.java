/**
 * 
 */
package com.apoollo.commons.server.spring.boot.starter.component.filter;

import java.io.IOException;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.apoollo.commons.server.spring.boot.starter.model.RequestContextHttpServletRequestWrapper;
import com.apoollo.commons.server.spring.boot.starter.model.Version;
import com.apoollo.commons.server.spring.boot.starter.properties.PathProperties;
import com.apoollo.commons.util.IpUtils;
import com.apoollo.commons.util.LangUtils;
import com.apoollo.commons.util.exception.AppClientRequestIdIllegalException;
import com.apoollo.commons.util.request.context.LoggerWriter;
import com.apoollo.commons.util.request.context.RequestContext;
import com.apoollo.commons.util.request.context.RequestContextInitail;
import com.apoollo.commons.util.request.context.access.RequestResource;
import com.apoollo.commons.util.request.context.access.SecurePrincipal;
import com.apoollo.commons.util.request.context.access.User;
import com.apoollo.commons.util.request.context.limiter.Limiters;
import com.apoollo.commons.util.request.context.limiter.support.LimitersSupport;
import com.apoollo.commons.util.request.context.model.RequestConstants;
import com.apoollo.commons.util.request.context.model.RequestContextCapacitySupport;
import com.apoollo.commons.util.request.context.model.ServletInputStreamHelper;

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
	private SecurePrincipal<RequestResource> secureRequestResource;
	private SecurePrincipal<User> secureUser;

	private LoggerWriter logWitter;
	private Limiters<LimitersSupport> limiters;

	public RequestContextFilter(PathProperties pathProperties, RequestContextInitail requestContextInitail,
			SecurePrincipal<RequestResource> secureRequestResource, SecurePrincipal<User> secureUser,
			LoggerWriter logWitter, Limiters<LimitersSupport> limiters,
			RequestContextCapacitySupport requestContextSupport) {
		super(pathProperties, requestContextSupport);
		this.requestContextInitail = requestContextInitail;
		this.secureRequestResource = secureRequestResource;
		this.secureUser = secureUser;
		this.logWitter = logWitter;
		this.limiters = limiters;
	}

	@Override
	public HttpServletRequest doPreSecureFilter(HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
		String requestId = LangUtils.getUppercaseUUID();
		String reuqestUri = request.getRequestURI();
		String requestIp = IpUtils.tryGetRealIp(request);
		RequestContext requestContext = RequestContext.reset(requestId, request.getContextPath(), reuqestUri,
				requestContextInitail::newInstance);
		LOGGER.info("请求进入标记");
		response.setHeader(RequestConstants.RESPONSE_HEADER_VERSION, Version.CURRENT_VERSION);
		String clientRequestId = StringUtils.trim(request.getHeader(RequestConstants.REQUEST_HEADER_REQUEST_ID));
		if (null != clientRequestId && (clientRequestId.length() > 32 || clientRequestId.length() < 1)) {
			throw new AppClientRequestIdIllegalException(
					"client request id illegal , value length must great equal 1 and less equal 32");
		}
		requestContext.setClientRequestId(clientRequestId);
		requestContext.setRequestIp(requestIp);
		requestContext.setRequestServerName(request.getServerName());
		if (null != clientRequestId) {
			LOGGER.info("客户端请求ID：" + clientRequestId);
		}
		LOGGER.info("访问URI：" + reuqestUri);
		LOGGER.info("访问IP：" + requestIp);
		requestContext.setRequestBody(ServletInputStreamHelper.getBodyByteArray(request));
		requestContextSupport.doSupport(capacitySupport -> {
			limiters.limit(request, response, requestContext, capacitySupport);
		});
		secureRequestResource.init(request, response, requestContext);
		User user = secureUser.init(request, response, requestContext);
		if (null != user) {
			response.setHeader(RequestConstants.RESPONSE_HEADER_USER_PASSWORD_EXPIRED,
					String.valueOf(user.passwordIsExpired()));
		}
		return new RequestContextHttpServletRequestWrapper(request, requestContext);
	}

	@Override
	public void cleanupMatches(HttpServletRequest request, HttpServletResponse response, FilterChain chain) {
		RequestContext requestContext = RequestContext.getRequired();
		try {
			requestContextSupport.doSupport(requestContext, capacitySupport -> {
				limiters.unlimit(request, response, requestContext, capacitySupport);
			});
		} catch (Exception e) {
			LOGGER.info("unlimit error", e);
		}
		try {
			logWitter.write(requestContext);
			if (null != requestContext.getResponseTime()) {
				LOGGER.info("total elapsedTime：" + requestContext.getElapsedTime() + "ms");
			} else {
				LOGGER.info("total elapsedTime："
						+ (System.currentTimeMillis() - requestContext.getRequestTime()) + "ms");
			}
		} catch (Exception e) {
			LOGGER.info("write log error", e);
		}
		LOGGER.info("请求结束标记");
		try {
			RequestContext.release();
			super.cleanAttribute(request);
		} catch (Exception e) {
			LOGGER.info("release requestContext error", e);
		}
	}

	@Override
	public void cleanupNoMaches(HttpServletRequest request, HttpServletResponse response, FilterChain chain) {
		super.cleanAttribute(request);
	}

}
