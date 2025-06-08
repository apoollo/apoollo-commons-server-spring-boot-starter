/**
 * 
 */
package com.apoollo.commons.server.spring.boot.starter.service.impl;

import java.util.List;

import com.apoollo.commons.server.spring.boot.starter.service.SecurePrincipal;
import com.apoollo.commons.util.JwtUtils.JwtToken;
import com.apoollo.commons.util.request.context.RequestContext;
import com.apoollo.commons.util.request.context.RequestResource;
import com.apoollo.commons.util.request.context.User;
import com.apoollo.commons.util.request.context.access.Authentication;
import com.apoollo.commons.util.request.context.access.Authorization;
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

		Authority<?> authority = authentications.stream()
				.filter(authentication -> authentication.support(requestResource.getAccessStrategy())).findAny().get()
				.authenticate(request);
		User user = authority.getUser();
		authorization.authorize(user, requestResource);
		Object token = authority.getToken();

		if (token instanceof JwtToken jwtToken) {
			authorizationRenewal.renewal(user, jwtToken, (renewal) -> {
				response.setHeader(RequestConstants.RESPONSE_HEADER_RENEWAL_AUTHORIZATION,
						renewal.getRenewalAuthorizationJwtToken());
			});
		}
		limiters.limit(request, response, requestContext, user);
		requestContext.setUser(user);
		return user;
	}

}
