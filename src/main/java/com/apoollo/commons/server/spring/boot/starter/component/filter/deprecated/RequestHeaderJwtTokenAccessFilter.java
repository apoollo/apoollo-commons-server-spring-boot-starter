/**
 * 
 */
package com.apoollo.commons.server.spring.boot.starter.component.filter.deprecated;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.apoollo.commons.server.spring.boot.starter.component.filter.AbstractSecureFilter;
import com.apoollo.commons.server.spring.boot.starter.properties.PathProperties;
import com.apoollo.commons.server.spring.boot.starter.service.Access;
import com.apoollo.commons.server.spring.boot.starter.service.AuthorizationJwtTokenJwtTokenDecoder;
import com.apoollo.commons.server.spring.boot.starter.service.impl.JwtAuthorizationRenewal;
import com.apoollo.commons.util.JwtUtils;
import com.apoollo.commons.util.JwtUtils.JwtToken;
import com.apoollo.commons.util.request.context.RequestContext;
import com.apoollo.commons.util.request.context.RequestResource;
import com.apoollo.commons.util.request.context.core.AccessStrategy;
import com.apoollo.commons.util.request.context.model.RequestConstants;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * @author liuyulong
 * @since 2025-05-22
 */
public class RequestHeaderJwtTokenAccessFilter extends AbstractSecureFilter {

	private static final Logger LOGGER = LoggerFactory.getLogger(RequestHeaderJwtTokenAccessFilter.class);

	private AuthorizationJwtTokenJwtTokenDecoder authorizationJwtTokenJwtTokenDecoder;
	private Access<JwtToken> access;
	private JwtAuthorizationRenewal authorizationRenewal;

	public RequestHeaderJwtTokenAccessFilter(PathProperties pathProperties,
			AuthorizationJwtTokenJwtTokenDecoder authorizationJwtTokenJwtTokenDecoder, Access<JwtToken> access,
			JwtAuthorizationRenewal authorizationRenewal) {
		super(pathProperties);
		this.authorizationJwtTokenJwtTokenDecoder = authorizationJwtTokenJwtTokenDecoder;
		this.access = access;
		this.authorizationRenewal = authorizationRenewal;
	}

	@Override
	public void doSecureFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws IOException, ServletException {

		RequestContext requestContext = RequestContext.getRequired();
		RequestResource requestResource = requestContext.getRequestResource();
		if (AccessStrategy.PRIVATE_HEADER_JWT_TOKEN == requestResource.getAccessStrategy()) {
			JwtToken jwtToken = authorizationJwtTokenJwtTokenDecoder
					.decode(request.getHeader(JwtUtils.HEADER_AUTHORIZATIONE));

			access.access(jwtToken.getAccessKey(), jwtToken);

			authorizationRenewal.renewal(RequestContext.getRequired().getUser(), jwtToken, (renewal) -> {
				response.setHeader(RequestConstants.RESPONSE_HEADER_RENEWAL_AUTHORIZATION,
						renewal.getRenewalAuthorizationJwtToken());
			});
			LOGGER.info("header jwt token validate accessed");
		}
		chain.doFilter(request, response);

	}

}
