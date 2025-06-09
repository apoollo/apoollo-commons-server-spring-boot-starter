/**
 * 
 */
package com.apoollo.commons.server.spring.boot.starter.service.impl;

import java.util.List;

import org.apache.commons.lang3.BooleanUtils;

import com.apoollo.commons.server.spring.boot.starter.service.SecurePrincipal;
import com.apoollo.commons.util.JwtUtils.JwtToken;
import com.apoollo.commons.util.request.context.RequestContext;
import com.apoollo.commons.util.request.context.access.Authentication;
import com.apoollo.commons.util.request.context.access.Authorization;
import com.apoollo.commons.util.request.context.access.RequestResource;
import com.apoollo.commons.util.request.context.access.User;
import com.apoollo.commons.util.request.context.access.core.AbstractAuthentication.Authority;
import com.apoollo.commons.util.request.context.limiter.Limiters;
import com.apoollo.commons.util.request.context.limiter.support.LimitersSupport;
import com.apoollo.commons.util.request.context.model.RequestConstants;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * liuyulong
 */
public class SecureUser implements SecurePrincipal<User> {

	private List<Authentication<?>> authentications;
	private Authorization authorization;
	private Limiters<LimitersSupport> limiters;
	private JwtAuthorizationRenewal authorizationRenewal;

	public SecureUser(List<Authentication<?>> authentications, Authorization authorization,
			Limiters<LimitersSupport> limiters, JwtAuthorizationRenewal authorizationRenewal) {
		super();
		this.authentications = authentications;
		this.authorization = authorization;
		this.limiters = limiters;
		this.authorizationRenewal = authorizationRenewal;
	}

	@Override
	public User init(HttpServletRequest request, HttpServletResponse response, RequestContext requestContext) {
		RequestResource requestResource = requestContext.getRequestResource();
		return authentications.stream()
				.filter(authentication -> authentication.support(requestResource.getAccessStrategy()))
				.map(authentication -> {
					Authority<?> authority = authentication.authenticate(request, requestContext);
					User user = authority.getUser();
					authorization.authorize(user, requestResource);
					Object token = authority.getToken();
					if (token instanceof JwtToken jwtToken) {
						authorizationRenewal.renewal(user, jwtToken, (renewal) -> {
							response.setHeader(RequestConstants.RESPONSE_HEADER_RENEWAL_AUTHORIZATION,
									renewal.getRenewalAuthorizationJwtToken());
						});
					}
					if (BooleanUtils.isNotFalse(user.getEnableCapacity())) {
						limiters.limit(request, response, requestContext, user);
					}
					requestContext.setUser(user);
					return user;
				})//
				.findAny()//
				.orElse(null);
	}

}
