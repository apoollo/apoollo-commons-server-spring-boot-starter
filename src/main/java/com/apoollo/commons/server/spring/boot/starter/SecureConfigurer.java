/**
 * 
 */
package com.apoollo.commons.server.spring.boot.starter;

import java.util.List;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.StringRedisTemplate;

import com.apoollo.commons.server.spring.boot.starter.properties.CommonsServerProperties;
import com.apoollo.commons.server.spring.boot.starter.properties.RabcProperties;
import com.apoollo.commons.server.spring.boot.starter.service.RequestResourceManager;
import com.apoollo.commons.server.spring.boot.starter.service.SecurePrincipal;
import com.apoollo.commons.server.spring.boot.starter.service.impl.DefaultRequestResourceManager;
import com.apoollo.commons.server.spring.boot.starter.service.impl.JwtAuthorizationRenewal;
import com.apoollo.commons.server.spring.boot.starter.service.impl.SecureRequestResource;
import com.apoollo.commons.server.spring.boot.starter.service.impl.SecureUser;
import com.apoollo.commons.util.JwtUtils.JwtToken;
import com.apoollo.commons.util.LangUtils;
import com.apoollo.commons.util.redis.service.RedisNameSpaceKey;
import com.apoollo.commons.util.request.context.access.Authentication;
import com.apoollo.commons.util.request.context.access.Authorization;
import com.apoollo.commons.util.request.context.access.AuthorizationJwtTokenDecoder;
import com.apoollo.commons.util.request.context.access.RequestResource;
import com.apoollo.commons.util.request.context.access.User;
import com.apoollo.commons.util.request.context.access.UserManager;
import com.apoollo.commons.util.request.context.access.UserRequestResourceMatcher;
import com.apoollo.commons.util.request.context.access.core.DefaultAuthenticationJwtTokenDecoder;
import com.apoollo.commons.util.request.context.access.core.DefaultAuthorization;
import com.apoollo.commons.util.request.context.access.core.DefaultUserManager;
import com.apoollo.commons.util.request.context.access.core.DefaultUserRequestResourceMatcher;
import com.apoollo.commons.util.request.context.access.core.HeaderJwtAuthentication;
import com.apoollo.commons.util.request.context.access.core.HeaderKeyPairAuthentication;
import com.apoollo.commons.util.request.context.access.core.JSONBodyJwtAuthentication;
import com.apoollo.commons.util.request.context.access.core.JSONBodyKeyPairAuthentication;
import com.apoollo.commons.util.request.context.access.core.ParameterKeyPairAuthentication;
import com.apoollo.commons.util.request.context.limiter.Limiters;
import com.apoollo.commons.util.request.context.limiter.support.LimitersSupport;
import com.apoollo.commons.util.web.spring.Instance;

/**
 * liuyulong
 */
@AutoConfiguration
@ConditionalOnWebApplication
public class SecureConfigurer {

	@Bean
	@ConditionalOnMissingBean
	RequestResourceManager getRequestResourceManager(Instance instance, StringRedisTemplate redisTemplate,
			RedisNameSpaceKey redisNameSpaceKey, CommonsServerProperties commonsServerProperties) {
		return new DefaultRequestResourceManager(instance, redisTemplate, redisNameSpaceKey, commonsServerProperties);
	}

	@Bean
	@ConditionalOnMissingBean
	UserManager getUserManager(Instance instance, StringRedisTemplate stringRedisTemplate,
			RedisNameSpaceKey redisNameSpaceKey, CommonsServerProperties commonsServerProperties) {
		return new DefaultUserManager(instance, stringRedisTemplate, redisNameSpaceKey,
				LangUtils.getPropertyIfNotNull(commonsServerProperties.getRbac(), (rbac) -> rbac.getUsers()));
	}

	@Bean
	@ConditionalOnMissingBean
	AuthorizationJwtTokenDecoder getAuthorizationJwtTokenJwtTokenDecoder() {
		return new DefaultAuthenticationJwtTokenDecoder();
	}

	@Bean
	@ConditionalOnMissingBean
	JwtAuthorizationRenewal getJwtAuthorizationRenewal(UserManager userManager) {
		return new JwtAuthorizationRenewal(userManager);
	}

	@Bean
	Authentication<JwtToken> getHeaderJwtTokenAuthentication(UserManager userManager,
			AuthorizationJwtTokenDecoder authorizationJwtTokenDecoder) {
		return new HeaderJwtAuthentication(userManager, authorizationJwtTokenDecoder);
	}

	@Bean
	Authentication<JwtToken> getJSONBodyJwtAuthentication(UserManager userManager,
			AuthorizationJwtTokenDecoder authorizationJwtTokenDecoder,
			CommonsServerProperties commonsServerProperties) {
		return new JSONBodyJwtAuthentication(userManager, authorizationJwtTokenDecoder,
				commonsServerProperties.getJwtTokenProperty());
	}

	@Bean
	Authentication<String> getHeaderKeyPairAuthentication(UserManager userManager,
			CommonsServerProperties commonsServerProperties) {
		return new HeaderKeyPairAuthentication(userManager, commonsServerProperties.getKeyPairAccessKeyProperty(),
				commonsServerProperties.getKeyPairSecretKeyProperty());
	}

	@Bean
	Authentication<String> getParameterKeyPairAuthentication(UserManager userManager,
			CommonsServerProperties commonsServerProperties) {
		return new ParameterKeyPairAuthentication(userManager, commonsServerProperties.getKeyPairAccessKeyProperty(),
				commonsServerProperties.getKeyPairSecretKeyProperty());
	}

	@Bean
	Authentication<String> getJSONBodyKeyPairAuthentication(UserManager userManager,
			CommonsServerProperties commonsServerProperties) {
		return new JSONBodyKeyPairAuthentication(userManager, commonsServerProperties.getKeyPairAccessKeyProperty(),
				commonsServerProperties.getKeyPairSecretKeyProperty());
	}

	@Bean
	@ConditionalOnMissingBean
	UserRequestResourceMatcher getUserRequestResourceMatcher() {
		return new DefaultUserRequestResourceMatcher();
	}

	@Bean
	@ConditionalOnMissingBean
	Authorization getAuthorization(StringRedisTemplate redisTemplate, RedisNameSpaceKey redisNameSpaceKey,
			UserRequestResourceMatcher requestResourceMatcher, CommonsServerProperties commonsServerProperties) {
		return new DefaultAuthorization(redisTemplate, redisNameSpaceKey, requestResourceMatcher,
				LangUtils.getPropertyIfNotNull(commonsServerProperties.getRbac(),
						RabcProperties::getAccessKeyAndRequestResourcePinsMapping));
	}

	@Bean
	SecurePrincipal<RequestResource> getSecureRequestResource(RequestResourceManager requestResourceManager,
			Limiters<LimitersSupport> limiters) {
		return new SecureRequestResource(requestResourceManager, limiters);
	}

	@Bean
	SecurePrincipal<User> getSecureUser(List<Authentication<?>> authentications, Authorization authorization,
			Limiters<User> limiters, JwtAuthorizationRenewal authorizationRenewal) {
		return new SecureUser(authentications, authorization, limiters, authorizationRenewal);
	}

}
