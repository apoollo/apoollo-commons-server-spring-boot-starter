/**
 * 
 */
package com.apoollo.commons.server.spring.boot.starter.service.impl;

import com.apoollo.commons.server.spring.boot.starter.service.Access;
import com.apoollo.commons.server.spring.boot.starter.service.AuthorizationJwtTokenJwtTokenDecoder;
import com.apoollo.commons.server.spring.boot.starter.service.SecurePrincipal;
import com.apoollo.commons.server.spring.boot.starter.service.UserManager;
import com.apoollo.commons.util.JwtUtils;
import com.apoollo.commons.util.JwtUtils.JwtToken;
import com.apoollo.commons.util.request.context.RequestContext;
import com.apoollo.commons.util.request.context.RequestResource;
import com.apoollo.commons.util.request.context.User;
import com.apoollo.commons.util.request.context.core.AccessStrategy;
import com.apoollo.commons.util.request.context.limiter.Limiters;
import com.apoollo.commons.util.request.context.limiter.support.LimitersSupport;
import com.apoollo.commons.util.request.context.model.RequestConstants;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * liuyulong
 */
public class SecureUser implements SecurePrincipal<User> {

	private UserManager userManager;
	private Limiters<LimitersSupport> limiters;
	private AuthorizationJwtTokenJwtTokenDecoder authorizationJwtTokenJwtTokenDecoder;
	private Access<JwtToken> access;
	private JwtAuthorizationRenewal authorizationRenewal;

	public SecureUser(UserManager userManager, Limiters<LimitersSupport> limiters) {
		super();
		this.userManager = userManager;
		this.limiters = limiters;
	}

	@Override
	public User init(HttpServletRequest request, HttpServletResponse response, RequestContext requestContext) {
		RequestResource requestResource = requestContext.getRequestResource();
		if (AccessStrategy.PRIVATE_HEADER_JWT_TOKEN == requestResource.getAccessStrategy()) {
			JwtToken jwtToken = authorizationJwtTokenJwtTokenDecoder
					.decode(request.getHeader(JwtUtils.HEADER_AUTHORIZATIONE));

			access.access(jwtToken.getAccessKey(), jwtToken);

			authorizationRenewal.renewal(RequestContext.getRequired().getUser(), jwtToken, (renewal) -> {
				response.setHeader(RequestConstants.RESPONSE_HEADER_RENEWAL_AUTHORIZATION,
						renewal.getRenewalAuthorizationJwtToken());
			});
		}

		return null;
	}

}
