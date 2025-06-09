/**
 * 
 */
package com.apoollo.commons.server.spring.boot.starter;

import java.util.List;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.StringRedisTemplate;

import com.apoollo.commons.server.spring.boot.starter.component.aspect.RequestResourceAspect;
import com.apoollo.commons.server.spring.boot.starter.component.bodyadvice.ExceptionControllerAdvice;
import com.apoollo.commons.server.spring.boot.starter.component.bodyadvice.ResponseBodyContextAdvice;
import com.apoollo.commons.server.spring.boot.starter.component.filter.RequestContentEscapeFilter;
import com.apoollo.commons.server.spring.boot.starter.component.filter.RequestContextFilter;
import com.apoollo.commons.server.spring.boot.starter.model.Constants;
import com.apoollo.commons.server.spring.boot.starter.properties.CommonsServerProperties;
import com.apoollo.commons.server.spring.boot.starter.properties.RabcProperties;
import com.apoollo.commons.server.spring.boot.starter.service.LoggerWriter;
import com.apoollo.commons.server.spring.boot.starter.service.RequestResourceManager;
import com.apoollo.commons.server.spring.boot.starter.service.SecurePrincipal;
import com.apoollo.commons.server.spring.boot.starter.service.impl.DefaultRequestResourceManager;
import com.apoollo.commons.server.spring.boot.starter.service.impl.JwtAuthorizationRenewal;
import com.apoollo.commons.server.spring.boot.starter.service.impl.SecureRequestResource;
import com.apoollo.commons.server.spring.boot.starter.service.impl.SecureUser;
import com.apoollo.commons.util.JwtUtils.JwtToken;
import com.apoollo.commons.util.LangUtils;
import com.apoollo.commons.util.redis.service.RedisNameSpaceKey;
import com.apoollo.commons.util.request.context.CapacitySupport;
import com.apoollo.commons.util.request.context.RequestContextInitail;
import com.apoollo.commons.util.request.context.RequestResource;
import com.apoollo.commons.util.request.context.User;
import com.apoollo.commons.util.request.context.access.Authentication;
import com.apoollo.commons.util.request.context.access.Authorization;
import com.apoollo.commons.util.request.context.access.AuthorizationJwtTokenDecoder;
import com.apoollo.commons.util.request.context.access.UserManager;
import com.apoollo.commons.util.request.context.access.core.DefaultAuthenticationJwtTokenDecoder;
import com.apoollo.commons.util.request.context.access.core.DefaultAuthorization;
import com.apoollo.commons.util.request.context.access.core.DefaultUserManager;
import com.apoollo.commons.util.request.context.access.core.HeaderJwtAuthentication;
import com.apoollo.commons.util.request.context.access.core.HeaderKeyPairAuthentication;
import com.apoollo.commons.util.request.context.access.core.ParameterKeyPairAuthentication;
import com.apoollo.commons.util.request.context.limiter.ContentEscapeHandler;
import com.apoollo.commons.util.request.context.limiter.Limiters;
import com.apoollo.commons.util.request.context.limiter.support.LimitersSupport;
import com.apoollo.commons.util.web.spring.Instance;

import jakarta.servlet.Filter;

/**
 * liuyulong
 */
@AutoConfiguration
public class ComponentConfigurer {

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
	@ConditionalOnMissingBean
	Authentication<JwtToken> getHeaderJwtTokenAuthentication(UserManager userManager,
			AuthorizationJwtTokenDecoder authorizationJwtTokenDecoder) {
		return new HeaderJwtAuthentication(userManager, authorizationJwtTokenDecoder);
	}

	@Bean
	@ConditionalOnMissingBean
	Authentication<String> getHeaderKeyPairAuthentication(UserManager userManager,
			CommonsServerProperties commonsServerProperties) {
		return new HeaderKeyPairAuthentication(userManager, commonsServerProperties.getKeyPairAccessKeyProperty(),
				commonsServerProperties.getKeyPairSecretKeyProperty());
	}

	@Bean
	@ConditionalOnMissingBean
	Authentication<String> getParameterKeyPairAuthentication(UserManager userManager,
			CommonsServerProperties commonsServerProperties) {
		return new ParameterKeyPairAuthentication(userManager, commonsServerProperties.getKeyPairAccessKeyProperty(),
				commonsServerProperties.getKeyPairSecretKeyProperty());
	}

	@Bean
	@ConditionalOnMissingBean
	SecurePrincipal<RequestResource> getSecureRequestResource(RequestResourceManager requestResourceManager,
			Limiters<LimitersSupport> limiters) {
		return new SecureRequestResource(requestResourceManager, limiters);
	}

	@Bean
	@ConditionalOnMissingBean
	Authorization getAuthorization(StringRedisTemplate redisTemplate, RedisNameSpaceKey redisNameSpaceKey,
			CommonsServerProperties commonsServerProperties) {
		return new DefaultAuthorization(redisTemplate, redisNameSpaceKey, LangUtils.getPropertyIfNotNull(
				commonsServerProperties.getRbac(), RabcProperties::getAccessKeyRequestResourcePinsMapping));
	}

	@ConditionalOnMissingBean
	SecurePrincipal<User> getSecureUser(List<Authentication<?>> authentications, Authorization authorization,
			Limiters<LimitersSupport> limiters, JwtAuthorizationRenewal authorizationRenewal) {
		return new SecureUser(authentications, authorization, limiters, authorizationRenewal);
	}

	@Bean
	FilterRegistrationBean<RequestContextFilter> getRequestContextFilterRegistrationBean(
			RequestContextInitail requestContextInitail, SecurePrincipal<RequestResource> secureRequestResource,
			SecurePrincipal<User> secureUser, LoggerWriter logWitter, Limiters<LimitersSupport> limiters,
			CapacitySupport capacitySupport, CommonsServerProperties commonsServerProperties) {
		return newFilterRegistrationBean(
				new RequestContextFilter(commonsServerProperties.getPath(), requestContextInitail,
						secureRequestResource, secureUser, logWitter, limiters, capacitySupport),
				Constants.REQUEST_CONTEXT_FILTER_ORDER);
	}

	/*
	 * @Bean FilterRegistrationBean<RequestResourceFilter>
	 * getRequestResourceFilterRegistrationBean( RequestResourceManager
	 * requestResourceManager, Limiters<LimitersSupport> limiters,
	 * CommonsServerProperties commonsServerProperties) { return
	 * newFilterRegistrationBean( new
	 * RequestResourceFilter(commonsServerProperties.getPath(),
	 * requestResourceManager, limiters), Constants.REQUEST_RESOURCE_FILTER_ORDER);
	 * }
	 * 
	 * @Bean FilterRegistrationBean<RequestNonceValidateFilter>
	 * getRequestNonceValidateFilterRegistrationBean( CommonsServerProperties
	 * commonsServerProperties, NonceValidator nonceValidator) { return
	 * newFilterRegistrationBean( new
	 * RequestNonceValidateFilter(commonsServerProperties.getPath(),
	 * nonceValidator), Constants.REQUEST_NONCE_VALIDATE_FILTER_ORDER); }
	 * 
	 * @Bean FilterRegistrationBean<RequestSignatureValidateFilter>
	 * getRequestSignatureValidateFilterRegistrationBean( CommonsServerProperties
	 * commonsServerProperties) { return newFilterRegistrationBean( new
	 * RequestSignatureValidateFilter(commonsServerProperties.getPath(),
	 * commonsServerProperties.getSignatureSecret()),
	 * Constants.REQUEST_SIGNATURE_VALIDATE_FILTER_ORDER); }
	 */
	@Bean
	FilterRegistrationBean<RequestContentEscapeFilter> getRequestContentEscapeFilterRegistrationBean(
			ContentEscapeHandler contentEscapeHandler, CommonsServerProperties commonsServerProperties) {
		return newFilterRegistrationBean(
				new RequestContentEscapeFilter(commonsServerProperties.getPath(), contentEscapeHandler),
				Constants.REQUEST_CONTENT_ESCAPE_FILTER_ORDER);
	}

	/*
	 * @Bean FilterRegistrationBean<RequestHeaderJwtTokenAccessFilter>
	 * getRequestHeaderJwtTokenAccessFilterRegistrationBean(
	 * AuthorizationJwtTokenDecoder authorizationJwtTokenJwtTokenDecoder,
	 * Access<JwtToken> access, JwtAuthorizationRenewal authorizationRenewal,
	 * CommonsServerProperties commonsServerProperties) { return
	 * newFilterRegistrationBean( new
	 * RequestHeaderJwtTokenAccessFilter(commonsServerProperties.getPath(),
	 * authorizationJwtTokenJwtTokenDecoder, access, authorizationRenewal),
	 * Constants.REQUEST_HEADER_JWT_TOKEN_ACCESS_FILTER_ORDER); }
	 * 
	 * @Bean FilterRegistrationBean<RequestHeaderKeyPairAccessFilter>
	 * getRequestHeaderKeyPairAccessFilterRegistrationBean( Access<String> access,
	 * CommonsServerProperties commonsServerProperties) { return
	 * newFilterRegistrationBean( new
	 * RequestHeaderKeyPairAccessFilter(commonsServerProperties.getPath(), access,
	 * commonsServerProperties.getKeyPairAccessKeyProperty(),
	 * commonsServerProperties.getKeyPairSecretKeyProperty()),
	 * Constants.REQUEST_HEADER_KEY_PAIR_ACCESS_FILTER_ORDER); }
	 * 
	 * @Bean FilterRegistrationBean<RequestParameterKeyPairAccessFilter>
	 * getRequestParameterKeyPairAccessFilterRegistrationBean( Access<String>
	 * access, CommonsServerProperties commonsServerProperties) { return
	 * newFilterRegistrationBean( new
	 * RequestParameterKeyPairAccessFilter(commonsServerProperties.getPath(),
	 * access, commonsServerProperties.getKeyPairAccessKeyProperty(),
	 * commonsServerProperties.getKeyPairSecretKeyProperty()),
	 * Constants.REQUEST_PARAMETER_KEY_PAIR_ACCESS_FILTER_ORDER); }
	 */

	private <T extends Filter> FilterRegistrationBean<T> newFilterRegistrationBean(T filter, int order) {
		FilterRegistrationBean<T> filterRegistrationBean = new FilterRegistrationBean<>();
		filterRegistrationBean.setFilter(filter);
		filterRegistrationBean.setOrder(order);
		filterRegistrationBean.setEnabled(true);
		filterRegistrationBean.setUrlPatterns(List.of("/*"));
		return filterRegistrationBean;
	}

	/*
	 * @Bean
	 * 
	 * @ConditionalOnMissingBean RequestBodyJwtTokenAccessAdvice
	 * getRequestBodyJwtTokenAccessAdvice( AuthorizationJwtTokenDecoder
	 * authorizationJwtTokenJwtTokenDecoder, Access<JwtToken> jwtTokenAccess) {
	 * return new
	 * RequestBodyJwtTokenAccessAdvice(authorizationJwtTokenJwtTokenDecoder,
	 * jwtTokenAccess); }
	 * 
	 * @Bean
	 * 
	 * @ConditionalOnMissingBean RequestBodyKeyPairAccessAdvice
	 * getRequestBodyKeyPairAccessAdvice(Access<String> secretKeyTokenAccess) {
	 * return new RequestBodyKeyPairAccessAdvice(secretKeyTokenAccess); }
	 */

	@Bean
	@ConditionalOnMissingBean
	ResponseBodyContextAdvice getResponseContextBodyAdvice() {
		return new ResponseBodyContextAdvice();
	}

	@Bean
	@ConditionalOnMissingBean
	ExceptionControllerAdvice getExceptionControllerAdvice() {
		return new ExceptionControllerAdvice();
	}

	@Bean
	@ConditionalOnMissingBean
	RequestResourceAspect getRequestResourceAspect() {
		return new RequestResourceAspect();
	}
}
