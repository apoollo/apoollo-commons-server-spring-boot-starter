/**
 * 
 */
package com.apoollo.commons.server.spring.boot.starter;

import java.util.List;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;

import com.apoollo.commons.server.spring.boot.starter.component.aspect.RequestResourceAspect;
import com.apoollo.commons.server.spring.boot.starter.component.bodyadvice.ExceptionControllerAdvice;
import com.apoollo.commons.server.spring.boot.starter.component.bodyadvice.RequestBodyJwtTokenAccessAdvice;
import com.apoollo.commons.server.spring.boot.starter.component.bodyadvice.RequestBodyKeyPairAccessAdvice;
import com.apoollo.commons.server.spring.boot.starter.component.bodyadvice.ResponseBodyContextAdvice;
import com.apoollo.commons.server.spring.boot.starter.component.filter.RequestContentEscapeFilter;
import com.apoollo.commons.server.spring.boot.starter.component.filter.RequestContextFilter;
import com.apoollo.commons.server.spring.boot.starter.component.filter.RequestHeaderJwtTokenAccessFilter;
import com.apoollo.commons.server.spring.boot.starter.component.filter.RequestHeaderKeyPairAccessFilter;
import com.apoollo.commons.server.spring.boot.starter.component.filter.RequestNonceValidateFilter;
import com.apoollo.commons.server.spring.boot.starter.component.filter.RequestParameterKeyPairAccessFilter;
import com.apoollo.commons.server.spring.boot.starter.component.filter.RequestResourceFilter;
import com.apoollo.commons.server.spring.boot.starter.component.filter.RequestSignatureValidateFilter;
import com.apoollo.commons.server.spring.boot.starter.model.Constants;
import com.apoollo.commons.server.spring.boot.starter.properties.CommonsServerProperties;
import com.apoollo.commons.server.spring.boot.starter.service.Access;
import com.apoollo.commons.server.spring.boot.starter.service.AuthorizationJwtTokenJwtTokenDecoder;
import com.apoollo.commons.server.spring.boot.starter.service.LoggerWriter;
import com.apoollo.commons.server.spring.boot.starter.service.RequestResourceManager;
import com.apoollo.commons.server.spring.boot.starter.service.UserManager;
import com.apoollo.commons.server.spring.boot.starter.service.impl.JwtAuthorizationRenewal;
import com.apoollo.commons.util.JwtUtils.JwtToken;
import com.apoollo.commons.util.request.context.CapacitySupport;
import com.apoollo.commons.util.request.context.NonceValidator;
import com.apoollo.commons.util.request.context.RequestContextInitail;
import com.apoollo.commons.util.request.context.WrapResponseHandler;
import com.apoollo.commons.util.request.context.core.DefaultWrapResponseHandler;
import com.apoollo.commons.util.request.context.limiter.ContentEscapeHandler;
import com.apoollo.commons.util.request.context.limiter.Limiters;
import com.apoollo.commons.util.request.context.limiter.support.LimitersSupport;

import jakarta.servlet.Filter;

/**
 * liuyulong
 */
@AutoConfiguration
public class ComponentConfigurer {

	@Bean
	@ConditionalOnMissingBean
	WrapResponseHandler getHttpCodeNameHandler() {
		return new DefaultWrapResponseHandler();
	}

	@Bean
	@ConditionalOnMissingBean
	JwtAuthorizationRenewal getJwtAuthorizationRenewal(UserManager userManager) {
		return new JwtAuthorizationRenewal(userManager);
	}

	@Bean
	FilterRegistrationBean<RequestContextFilter> getRequestContextFilterRegistrationBean(
			RequestContextInitail requestContextInitail, LoggerWriter logWitter, Limiters<LimitersSupport> limiters,
			CapacitySupport capacitySupport, CommonsServerProperties commonsServerProperties) {
		return newFilterRegistrationBean(new RequestContextFilter(commonsServerProperties.getPath(),
				requestContextInitail, logWitter, limiters, capacitySupport), Constants.REQUEST_CONTEXT_FILTER_ORDER);
	}

	@Bean
	FilterRegistrationBean<RequestResourceFilter> getRequestResourceFilterRegistrationBean(
			RequestResourceManager requestResourceManager, Limiters<LimitersSupport> limiters,
			CommonsServerProperties commonsServerProperties) {
		return newFilterRegistrationBean(
				new RequestResourceFilter(commonsServerProperties.getPath(), requestResourceManager, limiters),
				Constants.REQUEST_RESOURCE_FILTER_ORDER);
	}

	@Bean
	FilterRegistrationBean<RequestNonceValidateFilter> getRequestNonceValidateFilterRegistrationBean(
			CommonsServerProperties commonsServerProperties, NonceValidator nonceValidator) {
		return newFilterRegistrationBean(
				new RequestNonceValidateFilter(commonsServerProperties.getPath(), nonceValidator),
				Constants.REQUEST_NONCE_VALIDATE_FILTER_ORDER);
	}

	@Bean
	FilterRegistrationBean<RequestSignatureValidateFilter> getRequestSignatureValidateFilterRegistrationBean(
			CommonsServerProperties commonsServerProperties) {
		return newFilterRegistrationBean(
				new RequestSignatureValidateFilter(commonsServerProperties.getPath(),
						commonsServerProperties.getSignatureSecret()),
				Constants.REQUEST_SIGNATURE_VALIDATE_FILTER_ORDER);
	}

	@Bean
	FilterRegistrationBean<RequestContentEscapeFilter> getRequestContentEscapeFilterRegistrationBean(
			ContentEscapeHandler contentEscapeHandler, CommonsServerProperties commonsServerProperties) {
		return newFilterRegistrationBean(
				new RequestContentEscapeFilter(commonsServerProperties.getPath(), contentEscapeHandler),
				Constants.REQUEST_CONTENT_ESCAPE_FILTER_ORDER);
	}

	@Bean
	FilterRegistrationBean<RequestHeaderJwtTokenAccessFilter> getRequestHeaderJwtTokenAccessFilterRegistrationBean(
			AuthorizationJwtTokenJwtTokenDecoder authorizationJwtTokenJwtTokenDecoder, Access<JwtToken> access,
			JwtAuthorizationRenewal authorizationRenewal, CommonsServerProperties commonsServerProperties) {
		return newFilterRegistrationBean(
				new RequestHeaderJwtTokenAccessFilter(commonsServerProperties.getPath(),
						authorizationJwtTokenJwtTokenDecoder, access, authorizationRenewal),
				Constants.REQUEST_HEADER_JWT_TOKEN_ACCESS_FILTER_ORDER);
	}

	@Bean
	FilterRegistrationBean<RequestHeaderKeyPairAccessFilter> getRequestHeaderKeyPairAccessFilterRegistrationBean(
			Access<String> access, CommonsServerProperties commonsServerProperties) {
		return newFilterRegistrationBean(
				new RequestHeaderKeyPairAccessFilter(commonsServerProperties.getPath(), access,
						commonsServerProperties.getKeyPairAccessKeyProperty(),
						commonsServerProperties.getKeyPairSecretKeyProperty()),
				Constants.REQUEST_HEADER_KEY_PAIR_ACCESS_FILTER_ORDER);
	}

	@Bean
	FilterRegistrationBean<RequestParameterKeyPairAccessFilter> getRequestParameterKeyPairAccessFilterRegistrationBean(
			Access<String> access, CommonsServerProperties commonsServerProperties) {
		return newFilterRegistrationBean(
				new RequestParameterKeyPairAccessFilter(commonsServerProperties.getPath(), access,
						commonsServerProperties.getKeyPairAccessKeyProperty(),
						commonsServerProperties.getKeyPairSecretKeyProperty()),
				Constants.REQUEST_PARAMETER_KEY_PAIR_ACCESS_FILTER_ORDER);
	}

	private <T extends Filter> FilterRegistrationBean<T> newFilterRegistrationBean(T filter, int order) {
		FilterRegistrationBean<T> filterRegistrationBean = new FilterRegistrationBean<>();
		filterRegistrationBean.setFilter(filter);
		filterRegistrationBean.setOrder(order);
		filterRegistrationBean.setEnabled(true);
		filterRegistrationBean.setUrlPatterns(List.of("/*"));
		return filterRegistrationBean;
	}

	@Bean
	@ConditionalOnMissingBean
	RequestBodyJwtTokenAccessAdvice getRequestBodyJwtTokenAccessAdvice(
			AuthorizationJwtTokenJwtTokenDecoder authorizationJwtTokenJwtTokenDecoder,
			Access<JwtToken> jwtTokenAccess) {
		return new RequestBodyJwtTokenAccessAdvice(authorizationJwtTokenJwtTokenDecoder, jwtTokenAccess);
	}

	@Bean
	@ConditionalOnMissingBean
	RequestBodyKeyPairAccessAdvice getRequestBodyKeyPairAccessAdvice(Access<String> secretKeyTokenAccess) {
		return new RequestBodyKeyPairAccessAdvice(secretKeyTokenAccess);
	}

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
