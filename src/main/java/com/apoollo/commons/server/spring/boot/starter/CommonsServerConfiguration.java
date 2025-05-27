/**
 * 
 */
package com.apoollo.commons.server.spring.boot.starter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;

import com.apoollo.commons.server.spring.boot.starter.component.ApplicationReady;
import com.apoollo.commons.server.spring.boot.starter.component.CommonsServerWebMvcConfigurer;
import com.apoollo.commons.server.spring.boot.starter.component.aspect.RequestResourceAspect;
import com.apoollo.commons.server.spring.boot.starter.component.bodyadvice.ExceptionControllerAdvice;
import com.apoollo.commons.server.spring.boot.starter.component.bodyadvice.RequestBodyJwtTokenAccessAdvice;
import com.apoollo.commons.server.spring.boot.starter.component.bodyadvice.RequestBodyKeyPairAccessAdvice;
import com.apoollo.commons.server.spring.boot.starter.component.bodyadvice.ResponseBodyContextAdvice;
import com.apoollo.commons.server.spring.boot.starter.component.filter.RequestSignatureValidateFilter;
import com.apoollo.commons.server.spring.boot.starter.component.filter.RequestContentEscapeFilter;
import com.apoollo.commons.server.spring.boot.starter.component.filter.RequestContextFilter;
import com.apoollo.commons.server.spring.boot.starter.component.filter.RequestHeaderJwtTokenAccessFilter;
import com.apoollo.commons.server.spring.boot.starter.component.filter.RequestHeaderKeyPairAccessFilter;
import com.apoollo.commons.server.spring.boot.starter.component.filter.RequestParameterKeyPairAccessFilter;
import com.apoollo.commons.server.spring.boot.starter.component.filter.RequestResourceFilter;
import com.apoollo.commons.server.spring.boot.starter.controller.DynamicResourceController;
import com.apoollo.commons.server.spring.boot.starter.controller.ExceptionController;
import com.apoollo.commons.server.spring.boot.starter.controller.WelcomeController;
import com.apoollo.commons.server.spring.boot.starter.model.Constants;
import com.apoollo.commons.server.spring.boot.starter.properties.AccessProperties;
import com.apoollo.commons.server.spring.boot.starter.properties.CommonsServerProperties;
import com.apoollo.commons.server.spring.boot.starter.properties.PathProperties;
import com.apoollo.commons.server.spring.boot.starter.properties.RabcProperties;
import com.apoollo.commons.server.spring.boot.starter.service.Access;
import com.apoollo.commons.server.spring.boot.starter.service.AuthorizationJwtTokenJwtTokenDecoder;
import com.apoollo.commons.server.spring.boot.starter.service.CommonsServerRedisKey;
import com.apoollo.commons.server.spring.boot.starter.service.ContentEscapeHandler;
import com.apoollo.commons.server.spring.boot.starter.service.FlowLimiter;
import com.apoollo.commons.server.spring.boot.starter.service.LoggerWriter;
import com.apoollo.commons.server.spring.boot.starter.service.RequestResourceManager;
import com.apoollo.commons.server.spring.boot.starter.service.SyncService;
import com.apoollo.commons.server.spring.boot.starter.service.UserManager;
import com.apoollo.commons.server.spring.boot.starter.service.impl.DefaultAuthenticationJwtTokenDecoder;
import com.apoollo.commons.server.spring.boot.starter.service.impl.DefaultAuthorization;
import com.apoollo.commons.server.spring.boot.starter.service.impl.DefaultContentEscapeHandler;
import com.apoollo.commons.server.spring.boot.starter.service.impl.DefaultLoggerWriter;
import com.apoollo.commons.server.spring.boot.starter.service.impl.DefaultRequestContextDataBus;
import com.apoollo.commons.server.spring.boot.starter.service.impl.DefaultRequestResourceManager;
import com.apoollo.commons.server.spring.boot.starter.service.impl.DefaultSyncService;
import com.apoollo.commons.server.spring.boot.starter.service.impl.DefaultUserManager;
import com.apoollo.commons.server.spring.boot.starter.service.impl.JwtAuthorizationRenewal;
import com.apoollo.commons.server.spring.boot.starter.service.impl.JwtTokenAccess;
import com.apoollo.commons.server.spring.boot.starter.service.impl.SecretKeyTokenAccess;
import com.apoollo.commons.server.spring.boot.starter.service.impl.SlidingWindowLimiterImpl;
import com.apoollo.commons.util.JwtUtils.JwtToken;
import com.apoollo.commons.util.LangUtils;
import com.apoollo.commons.util.exception.AppServerOverloadedException;
import com.apoollo.commons.util.redis.service.CountLimiter;
import com.apoollo.commons.util.redis.service.RedisNameSpaceKey;
import com.apoollo.commons.util.redis.service.SlidingWindowLimiter;
import com.apoollo.commons.util.redis.service.impl.CommonsCountLimiter;
import com.apoollo.commons.util.redis.service.impl.CommonsSlidingWindowLimiter;
import com.apoollo.commons.util.request.context.Authorization;
import com.apoollo.commons.util.request.context.EscapeMethod;
import com.apoollo.commons.util.request.context.HttpCodeNameHandler;
import com.apoollo.commons.util.request.context.RequestContextDataBus;
import com.apoollo.commons.util.request.context.RequestContextInitail;
import com.apoollo.commons.util.request.context.def.DefaultEscapeXss;
import com.apoollo.commons.util.request.context.def.DefaultHttpCodeNameHandler;
import com.apoollo.commons.util.web.captcha.CaptchaService;
import com.apoollo.commons.util.web.captcha.RedisCaptchaService;
import com.github.benmanes.caffeine.cache.Caffeine;

import jakarta.servlet.Filter;

/**
 * @author liuyulong
 * @since 2023年8月22日
 */
@EnableCaching
@Configuration(proxyBeanMethods = true)
@ConditionalOnWebApplication
@ConditionalOnProperty(prefix = Constants.CONFIGURATION_PREFIX, name = "enable", matchIfMissing = true)
@Import({ CommonsServerWebMvcConfigurer.class, ApplicationReady.class, WelcomeController.class,
		ExceptionController.class, DynamicResourceController.class })
public class CommonsServerConfiguration {

	@Bean
	@ConfigurationProperties(Constants.CONFIGURATION_PREFIX)
	CommonsServerProperties getCommonsServerProperties() {
		PathProperties pathProperties = new PathProperties();
		pathProperties.setExcludePathPatterns(new ArrayList<>());
		pathProperties.setIncludePathPatterns(new ArrayList<>());

		RabcProperties rabcProperties = new RabcProperties();
		rabcProperties.setRequestResources(new ArrayList<>());

		CommonsServerProperties commonsServerProperties = new CommonsServerProperties();
		commonsServerProperties.setEnable(true);
		commonsServerProperties.setPath(pathProperties);
		commonsServerProperties.setAccess(new AccessProperties(null, null, true));

		commonsServerProperties.setRbac(rabcProperties);
		return commonsServerProperties;
	}

	@Bean
	@ConditionalOnMissingBean
	RequestContextInitail getRequestContextInitail() {
		return RequestContextInitail.DefaultRequestContext();
	}

	@Bean
	@Order(Ordered.HIGHEST_PRECEDENCE)
	RequestContextDataBus getDefaultRequesContextDataBus() {
		return new DefaultRequestContextDataBus();
	}

	@Bean
	@ConditionalOnMissingBean
	LoggerWriter getLoggerWriter(List<RequestContextDataBus> requestContextDataBuses) {
		return new DefaultLoggerWriter(requestContextDataBuses);
	}

	@Bean
	@ConditionalOnMissingBean
	RedisNameSpaceKey getRedisNameSpaceKey() {
		return () -> "unallocated-namespace";
	}

	@Bean
	@ConditionalOnMissingBean
	CommonsServerRedisKey getCommonsServerRedisKey(RedisNameSpaceKey redisNameSpaceKey) {
		return () -> redisNameSpaceKey;
	}

	@Bean
	@ConditionalOnMissingBean
	CaptchaService getCaptchaService(StringRedisTemplate redisTemplate, RedisNameSpaceKey redisNameSpaceKey) {
		return new RedisCaptchaService(redisTemplate, redisNameSpaceKey);
	}

	@Bean
	@ConditionalOnMissingBean
	SlidingWindowLimiter getSlidingWindowLimiter(RedisTemplate<String, String> redisTemplate,
			RedisNameSpaceKey redisNameSpaceKey) {
		return new CommonsSlidingWindowLimiter(redisTemplate, redisNameSpaceKey);
	}

	@Bean
	@ConditionalOnMissingBean
	FlowLimiter getFlowLimiter(SlidingWindowLimiter slidingWindowLimiter) {
		return new SlidingWindowLimiterImpl(slidingWindowLimiter);
	}

	@Bean
	@ConditionalOnMissingBean
	SyncService getSyncService(RedisTemplate<String, String> redisTemplate,
			CommonsServerRedisKey commonsServerRedisKey) {
		return new DefaultSyncService(redisTemplate, commonsServerRedisKey);
	}

	@Bean
	@ConditionalOnMissingBean
	RequestResourceManager getRequestResourceManager(StringRedisTemplate redisTemplate,
			CommonsServerRedisKey commonsServerRedisKey, CommonsServerProperties commonsServerProperties) {
		return new DefaultRequestResourceManager(redisTemplate, commonsServerRedisKey, commonsServerProperties);
	}

	@Bean
	@ConditionalOnMissingBean
	UserManager getUserManager(StringRedisTemplate stringRedisTemplate, CommonsServerRedisKey commonsServerRedisKey,
			CommonsServerProperties commonsServerProperties) {
		return new DefaultUserManager(stringRedisTemplate, commonsServerRedisKey,
				LangUtils.getPropertyIfNotNull(commonsServerProperties.getRbac(), (rbac) -> rbac.getUsers()));
	}

	@Bean
	@ConditionalOnMissingBean
	Authorization<?> getAuthorization(StringRedisTemplate stringRedisTemplate,
			CommonsServerRedisKey commonsServerRedisKey, CommonsServerProperties commonsServerProperties) {
		return new DefaultAuthorization(stringRedisTemplate, commonsServerRedisKey,
				LangUtils.getPropertyIfNotNull(commonsServerProperties.getRbac(), (rbac) -> rbac.getPermissions()));
	}

	@Bean
	@ConditionalOnMissingBean
	CountLimiter getCountLimiter(StringRedisTemplate stringRedisTemplate) {
		return new CommonsCountLimiter(stringRedisTemplate);
	}

	@Bean
	@ConditionalOnMissingBean
	AuthorizationJwtTokenJwtTokenDecoder getAuthorizationJwtTokenJwtTokenDecoder() {
		return new DefaultAuthenticationJwtTokenDecoder();
	}

	@Bean
	@ConditionalOnMissingBean(name = "jwtTokenAccess")
	Access<JwtToken> getJwtTokenAccess(UserManager userManager, Authorization<?> authorization,
			CommonsServerRedisKey commonsServerRedisKey, CountLimiter countLimiter, FlowLimiter FlowLimiter,
			CommonsServerProperties commonsServerProperties) {
		return new JwtTokenAccess(userManager, authorization, commonsServerRedisKey, countLimiter, FlowLimiter,
				commonsServerProperties.getAccess());
	}

	@Bean
	@ConditionalOnMissingBean(name = "secretKeyTokenAccess")
	Access<String> getSecretKeyTokenAccess(UserManager userManager, Authorization<?> authorization,
			CommonsServerRedisKey commonsServerRedisKey, CountLimiter countLimiter, FlowLimiter flowLimiter,
			CommonsServerProperties commonsServerProperties) {
		return new SecretKeyTokenAccess(userManager, authorization, commonsServerRedisKey, countLimiter, flowLimiter,
				commonsServerProperties.getAccess());
	}

	@Bean(name = "threadPoolExecutorForTimeOut", destroyMethod = "shutdown")
	@ConditionalOnMissingBean
	ThreadPoolExecutor getThreadPoolExecutorForTimeOut() {
		ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(500, 1200, 60L, TimeUnit.SECONDS,
				new LinkedBlockingQueue<Runnable>(), Executors.defaultThreadFactory(), (r, e) -> {
					throw new AppServerOverloadedException("Task " + r.toString() + " rejected from " + e.toString());
				});
		return threadPoolExecutor;
	}

	@Bean
	CacheManager cacheManager() {
		CaffeineCacheManager cacheManager = new CaffeineCacheManager();
		cacheManager.setCaffeine(Caffeine.newBuilder()//
				.expireAfterWrite(5, TimeUnit.MINUTES)//
				.initialCapacity(500)//
				.maximumSize(2000)//
		);
		return cacheManager;
	}

	@Bean
	@ConditionalOnMissingBean
	HttpCodeNameHandler getHttpCodeNameHandler() {
		return new DefaultHttpCodeNameHandler();
	}

	@Bean
	@ConditionalOnMissingBean
	JwtAuthorizationRenewal getJwtAuthorizationRenewal(UserManager userManager) {
		return new JwtAuthorizationRenewal(userManager);
	}

	@Bean
	@ConditionalOnMissingBean
	EscapeMethod getEscapeXss() {
		return new DefaultEscapeXss();
	}

	@Bean
	@ConditionalOnMissingBean
	ContentEscapeHandler getXssHandler(EscapeMethod escapeXss) {
		return new DefaultContentEscapeHandler(escapeXss);
	}


	@Bean
	FilterRegistrationBean<RequestContextFilter> getRequestContextFilterRegistrationBean(
			RequestContextInitail requestContextInitail, CountLimiter countLimiter, LoggerWriter logWitter,
			CommonsServerProperties commonsServerProperties) {
		return newFilterRegistrationBean(new RequestContextFilter(commonsServerProperties.getPath(),
				requestContextInitail, countLimiter, logWitter), Constants.REQUEST_CONTEXT_FILTER_ORDER);
	}

	@Bean
	FilterRegistrationBean<RequestResourceFilter> getRequestResourceFilterRegistrationBean(
			RequestResourceManager requestResourceManager, FlowLimiter flowLimiter, SyncService syncService,
			CommonsServerProperties commonsServerProperties) {
		return newFilterRegistrationBean(new RequestResourceFilter(commonsServerProperties.getPath(),
				requestResourceManager, flowLimiter, syncService), Constants.REQUEST_RESOURCE_FILTER_ORDER);
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
	public RequestResourceAspect getRequestResourceAspect() {
		return new RequestResourceAspect();
	}
}
