/**
 * 
 */
package com.apoollo.commons.server.spring.boot.starter.component.filter;

import java.io.IOException;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.apoollo.commons.server.spring.boot.starter.model.RequestContextHttpServletRequestWrapper;
import com.apoollo.commons.server.spring.boot.starter.model.Version;
import com.apoollo.commons.server.spring.boot.starter.properties.PathProperties;
import com.apoollo.commons.server.spring.boot.starter.service.LoggerWriter;
import com.apoollo.commons.server.spring.boot.starter.service.SecurePrincipal;
import com.apoollo.commons.util.IpUtils;
import com.apoollo.commons.util.LangUtils;
import com.apoollo.commons.util.request.context.RequestContext;
import com.apoollo.commons.util.request.context.RequestContextInitail;
import com.apoollo.commons.util.request.context.access.RequestResource;
import com.apoollo.commons.util.request.context.access.User;
import com.apoollo.commons.util.request.context.limiter.Limiters;
import com.apoollo.commons.util.request.context.limiter.support.CapacitySupport;
import com.apoollo.commons.util.request.context.limiter.support.LimitersSupport;
import com.apoollo.commons.util.request.context.model.RequestConstants;
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
	private CapacitySupport capacitySupport;

	public RequestContextFilter(PathProperties pathProperties, RequestContextInitail requestContextInitail,
			SecurePrincipal<RequestResource> secureRequestResource, SecurePrincipal<User> secureUser,
			LoggerWriter logWitter, Limiters<LimitersSupport> limiters, CapacitySupport capacitySupport) {
		super(pathProperties);
		this.requestContextInitail = requestContextInitail;
		this.secureRequestResource = secureRequestResource;
		this.secureUser = secureUser;
		this.logWitter = logWitter;
		this.limiters = limiters;
		this.capacitySupport = capacitySupport;
		if (StringUtils.isBlank(this.capacitySupport.getResourcePin())) {
			throw new RuntimeException("resourcePin must not be blank");
		}
	}

	@Override
	public void doSecureFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		LOGGER.info("请求进入标记");
		String clientRequestId = request.getHeader(RequestConstants.REQUEST_HEADER_REQUEST_ID);

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
		requestContext.setRequestBody(ServletInputStreamHelper.getBodyByteArray(request));
		CapacitySupport.doSupport(List.of(capacitySupport), capacitySupport -> {
			limiters.limit(request, response, requestContext, capacitySupport);
		});
		RequestResource requestResource = secureRequestResource.init(request, response, requestContext);
		User user = secureUser.init(request, response, requestContext);
		chain.doFilter(new RequestContextHttpServletRequestWrapper(request, requestContext), response);
		if (null != user) {
			response.setHeader(RequestConstants.RESPONSE_HEADER_USER_PASSWORD_EXPIRED,
					String.valueOf(user.passwordIsExpired()));
		}
		response.setHeader(RequestConstants.RESPONSE_HEADER_VERSION, Version.CURRENT_VERSION);
		logWitter.write(requestContext, () -> {
			LOGGER.info("请求结束标记");
		});
		CapacitySupport.doSupport(LangUtils.getStream(capacitySupport, user, requestResource).toList(),
				capacitySupport -> {
					limiters.unlimit(request, response, requestContext, user);
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
