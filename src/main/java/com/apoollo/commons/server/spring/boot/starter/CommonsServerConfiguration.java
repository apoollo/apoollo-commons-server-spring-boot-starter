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

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;

import com.apoollo.commons.util.JwtUtils.JwtToken;
import com.apoollo.commons.server.spring.boot.starter.component.ApplicationReady;
import com.apoollo.commons.server.spring.boot.starter.component.CommonsServerWebMvcConfigurer;
import com.apoollo.commons.server.spring.boot.starter.component.bodyadvice.ExceptionControllerAdvice;
import com.apoollo.commons.server.spring.boot.starter.component.bodyadvice.RequestBodyDigestValidateAdvice;
import com.apoollo.commons.server.spring.boot.starter.component.bodyadvice.RequestBodyJwtTokenAccessAdvice;
import com.apoollo.commons.server.spring.boot.starter.component.bodyadvice.RequestBodyKeepParameterAdvice;
import com.apoollo.commons.server.spring.boot.starter.component.bodyadvice.RequestBodySecretKeyTokenAccessAdvice;
import com.apoollo.commons.server.spring.boot.starter.component.bodyadvice.ResponseBodyContextAdvice;
import com.apoollo.commons.server.spring.boot.starter.component.filter.XssFilter;
import com.apoollo.commons.server.spring.boot.starter.component.interceptor.RequestContextInterceptor;
import com.apoollo.commons.server.spring.boot.starter.component.interceptor.RequestHeaderJwtTokenAccessInterceptor;
import com.apoollo.commons.server.spring.boot.starter.component.interceptor.RequestResourceInterceptor;
import com.apoollo.commons.server.spring.boot.starter.component.interceptor.RequestSecretKeyTokenAccessInterceptor;
import com.apoollo.commons.server.spring.boot.starter.controller.DynamicResourceController;
import com.apoollo.commons.server.spring.boot.starter.controller.WelcomeController;
import com.apoollo.commons.server.spring.boot.starter.model.Constants;
import com.apoollo.commons.server.spring.boot.starter.model.annotaion.RequestResourceAspect;
import com.apoollo.commons.server.spring.boot.starter.properties.AccessProperties;
import com.apoollo.commons.server.spring.boot.starter.properties.CacheProperties;
import com.apoollo.commons.server.spring.boot.starter.properties.CommonsServerProperties;
import com.apoollo.commons.server.spring.boot.starter.properties.EnablePorperties;
import com.apoollo.commons.server.spring.boot.starter.properties.FilterProperties;
import com.apoollo.commons.server.spring.boot.starter.properties.InterceptorCommonsProperties;
import com.apoollo.commons.server.spring.boot.starter.properties.RabcProperties;
import com.apoollo.commons.server.spring.boot.starter.properties.RequestContextInterceptorProperties;
import com.apoollo.commons.server.spring.boot.starter.properties.RequestHeaderJwtTokenAccessInterceptorProperties;
import com.apoollo.commons.server.spring.boot.starter.properties.RequestResourceInterceptorProperties;
import com.apoollo.commons.server.spring.boot.starter.service.Access;
import com.apoollo.commons.server.spring.boot.starter.service.AuthorizationJwtTokenJwtTokenDecoder;
import com.apoollo.commons.server.spring.boot.starter.service.CommonsServerRedisKey;
import com.apoollo.commons.server.spring.boot.starter.service.FlowLimiter;
import com.apoollo.commons.server.spring.boot.starter.service.InternalHandlerInterceptor;
import com.apoollo.commons.server.spring.boot.starter.service.LoggerWriter;
import com.apoollo.commons.server.spring.boot.starter.service.RequestResourceManager;
import com.apoollo.commons.server.spring.boot.starter.service.SyncService;
import com.apoollo.commons.server.spring.boot.starter.service.UserManager;
import com.apoollo.commons.server.spring.boot.starter.service.XssHandler;
import com.apoollo.commons.server.spring.boot.starter.service.impl.DefaultAuthenticationJwtTokenDecoder;
import com.apoollo.commons.server.spring.boot.starter.service.impl.DefaultAuthorization;
import com.apoollo.commons.server.spring.boot.starter.service.impl.DefaultLoggerWriter;
import com.apoollo.commons.server.spring.boot.starter.service.impl.DefaultRequestContextDataBus;
import com.apoollo.commons.server.spring.boot.starter.service.impl.DefaultRequestResourceManager;
import com.apoollo.commons.server.spring.boot.starter.service.impl.DefaultSyncService;
import com.apoollo.commons.server.spring.boot.starter.service.impl.DefaultUserManager;
import com.apoollo.commons.server.spring.boot.starter.service.impl.DefaultXssHandler;
import com.apoollo.commons.server.spring.boot.starter.service.impl.JwtAuthorizationRenewal;
import com.apoollo.commons.server.spring.boot.starter.service.impl.JwtTokenAccess;
import com.apoollo.commons.server.spring.boot.starter.service.impl.SecretKeyTokenAccess;
import com.apoollo.commons.server.spring.boot.starter.service.impl.SlidingWindowLimiterImpl;
import com.apoollo.commons.util.LangUtils;
import com.apoollo.commons.util.exception.AppServerOverloadedException;
import com.apoollo.commons.util.redis.service.CountLimiter;
import com.apoollo.commons.util.redis.service.RedisNameSpaceKey;
import com.apoollo.commons.util.redis.service.SlidingWindowLimiter;
import com.apoollo.commons.util.redis.service.impl.CommonsCountLimiter;
import com.apoollo.commons.util.redis.service.impl.CommonsSlidingWindowLimiter;
import com.apoollo.commons.util.request.context.Authorization;
import com.apoollo.commons.util.request.context.RequestContextDataBus;
import com.apoollo.commons.util.request.context.RequestContextInitail;
import com.apoollo.commons.util.web.captcha.CaptchaService;
import com.apoollo.commons.util.web.captcha.RedisCaptchaService;
import com.github.benmanes.caffeine.cache.Caffeine;

/**
 * @author liuyulong
 * @since 2023年8月22日
 */
@Configuration(proxyBeanMethods = true)
@ConditionalOnWebApplication
@ConditionalOnProperty(prefix = Constants.CONFIGURATION_PREFIX, name = "enable", matchIfMissing = true)
@Import({ ApplicationReady.class, WelcomeController.class, DynamicResourceController.class })
public class CommonsServerConfiguration {

	@Bean
	@ConfigurationProperties(Constants.CONFIGURATION_PREFIX)
	CommonsServerProperties getCommonsServerProperties() {

		InterceptorCommonsProperties commonsProperties = new InterceptorCommonsProperties();
		commonsProperties.setPathPatterns(new ArrayList<>());

		RabcProperties rabcProperties = new RabcProperties();
		rabcProperties.setRequestResources(new ArrayList<>());

		CommonsServerProperties commonsServerProperties = new CommonsServerProperties();

		commonsServerProperties.setEnable(true);
		commonsServerProperties.setRequestContextInterceptor(new RequestContextInterceptorProperties(true));
		commonsServerProperties.setRequestResourceInterceptor(new RequestResourceInterceptorProperties(true));
		commonsServerProperties
				.setRequestHeaderJwtTokenAccessInterceptor(new RequestHeaderJwtTokenAccessInterceptorProperties(true));
		commonsServerProperties.setResponseBodyContext(new EnablePorperties(true));
		commonsServerProperties.setCache(new CacheProperties(true));
		commonsServerProperties.setAccess(new AccessProperties(null, null, true));

		commonsServerProperties.setCommonsIntercetptor(commonsProperties);
		commonsServerProperties.setRbac(rabcProperties);
		return commonsServerProperties;
	}

	// @Bean
	// CacheManagerService getCacheManagerService(@Nullable CacheManager
	// cacheManager) {
	// return new DefaultCacheManagerService(cacheManager);
	// }

	@Bean
	CommonsServerWebMvcConfigurer getCommonsServerWebMvcConfigurer(ApplicationContext applicationContext,
			CommonsServerProperties commonsServerProperties,
			List<InternalHandlerInterceptor> internalHandlerInterceptors) {
		return new CommonsServerWebMvcConfigurer(applicationContext, commonsServerProperties,
				internalHandlerInterceptors);
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

	@Configuration(proxyBeanMethods = true)
	@ConditionalOnProperty(prefix = Constants.CONFIGURATION_PREFIX
			+ ".request-context-interceptor", name = "enable", matchIfMissing = true)
	public static class RequestContextInterceptorConfiguration {
		@Bean
		RequestContextInterceptor getRequestContextInterceptor(CommonsServerProperties commonsServerProperties,
				RequestContextInitail requestContextInitail, ThreadPoolExecutor threadPoolExecutorForTimeOut,
				CountLimiter countLimiter, LoggerWriter logWitter) {
			return new RequestContextInterceptor(commonsServerProperties.getRequestContextInterceptor(),
					requestContextInitail, threadPoolExecutorForTimeOut, countLimiter, logWitter);
		}

	}

	@Configuration(proxyBeanMethods = true)
	@ConditionalOnProperty(prefix = Constants.CONFIGURATION_PREFIX
			+ ".request-resource-interceptor", name = "enable", matchIfMissing = true)
	public static class RequestResourceInterceptorConfiguration {

		@Bean
		RequestResourceInterceptor getRequestResourceInterceptor(CommonsServerProperties commonsServerProperties,
				RequestResourceManager requestResourceManager, FlowLimiter flowLimiter, SyncService syncService) {
			return new RequestResourceInterceptor(commonsServerProperties.getRequestResourceInterceptor(),
					requestResourceManager, flowLimiter, syncService);
		}
	}

	@Configuration(proxyBeanMethods = true)
	@ConditionalOnProperty(prefix = Constants.CONFIGURATION_PREFIX
			+ ".request-header-jwt-token-access-interceptor", name = "enable", matchIfMissing = true)
	public static class RequestHeaderJwtTokenAccessInterceptorConfiguration {

		@Bean
		@ConditionalOnMissingBean
		JwtAuthorizationRenewal getJwtAuthorizationRenewal(UserManager userManager) {
			return new JwtAuthorizationRenewal(userManager);
		}

		@Bean
		RequestHeaderJwtTokenAccessInterceptor getRequestHeaderJwtTokenAccessInterceptor(
				CommonsServerProperties commonsServerProperties,
				AuthorizationJwtTokenJwtTokenDecoder authorizationJwtTokenJwtTokenDecoder,
				Access<JwtToken> jwtTokenAccess, JwtAuthorizationRenewal authorizationRenewal) {
			return new RequestHeaderJwtTokenAccessInterceptor(
					commonsServerProperties.getRequestHeaderJwtTokenAccessInterceptor(),
					authorizationJwtTokenJwtTokenDecoder, jwtTokenAccess, authorizationRenewal);
		}
	}

	@Configuration(proxyBeanMethods = true)
	@ConditionalOnProperty(prefix = Constants.CONFIGURATION_PREFIX
			+ ".request-secret-key-token-access-interceptor", name = "enable", matchIfMissing = false)
	public static class RequestSecretKeyTokenAccessInterceptorConfiguration {

		@Bean
		RequestSecretKeyTokenAccessInterceptor getRequestSecretKeyTokenAccessInterceptor(
				CommonsServerProperties commonsServerProperties, Access<String> secretKeyTokenAccess) {
			return new RequestSecretKeyTokenAccessInterceptor(
					commonsServerProperties.getRequestSecretKeyTokenAccessInterceptor(), secretKeyTokenAccess);
		}
	}

	@Configuration(proxyBeanMethods = true)
	@ConditionalOnProperty(prefix = Constants.CONFIGURATION_PREFIX
			+ ".request-body-keep-parameter-advice", name = "enable", matchIfMissing = false)
	public static class RequestBodyKeepParameterAdviceConfiguration {

		@Bean
		RequestBodyKeepParameterAdvice getRequestBodyKeepParameterAdvice() {
			return new RequestBodyKeepParameterAdvice();
		}

	}

	@Configuration(proxyBeanMethods = true)
	@ConditionalOnProperty(prefix = Constants.CONFIGURATION_PREFIX
			+ ".request-body-digest-validate-advice", name = "enable", matchIfMissing = false)
	public static class RequestBodyDigestValidateAdviceConfiguration {

		@Bean
		RequestBodyDigestValidateAdvice getRequestBodyDigestValidateAdvice(
				CommonsServerProperties commonsServerProperties) {
			return new RequestBodyDigestValidateAdvice(commonsServerProperties.getRequestBodyDigestValidateAdvice());
		}

	}

	@Configuration(proxyBeanMethods = true)
	@ConditionalOnProperty(prefix = Constants.CONFIGURATION_PREFIX
			+ ".request-body-jwt-token-access-advice", name = "enable", matchIfMissing = false)
	public static class RequestBodyJwtTokenAccessAdviceConfiguration {

		@Bean
		RequestBodyJwtTokenAccessAdvice getRequestBodyJwtTokenAccessAdvice(
				AuthorizationJwtTokenJwtTokenDecoder authorizationJwtTokenJwtTokenDecoder,
				Access<JwtToken> jwtTokenAccess) {
			return new RequestBodyJwtTokenAccessAdvice(authorizationJwtTokenJwtTokenDecoder, jwtTokenAccess);
		}
	}

	@Configuration(proxyBeanMethods = true)
	@ConditionalOnProperty(prefix = Constants.CONFIGURATION_PREFIX
			+ ".request-body-secret-key-token-access-advice", name = "enable", matchIfMissing = false)
	public static class RequestBodySecretKeyTokenAccessAdviceConfiguration {

		@Bean
		RequestBodySecretKeyTokenAccessAdvice getRequestBodySecretKeyTokenAccessAdvice(
				Access<String> secretKeyTokenAccess) {
			return new RequestBodySecretKeyTokenAccessAdvice(secretKeyTokenAccess);
		}
	}

	@Configuration(proxyBeanMethods = true)
	@ConditionalOnProperty(prefix = Constants.CONFIGURATION_PREFIX
			+ ".response-body-context", name = "enable", matchIfMissing = true)
	public static class ResponseContextBodyAdviceConfiguration {

		@Bean
		ResponseBodyContextAdvice getResponseContextBodyAdvice() {
			return new ResponseBodyContextAdvice();
		}

		@Bean
		ExceptionControllerAdvice getExceptionControllerAdvice() {
			return new ExceptionControllerAdvice();
		}

	}

	@EnableCaching
	@Configuration(proxyBeanMethods = true)
	@ConditionalOnProperty(prefix = Constants.CONFIGURATION_PREFIX + ".cache", name = "enable", matchIfMissing = true)
	public static class CacheConfig {

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
	}

	@Configuration(proxyBeanMethods = true)
	@ConditionalOnProperty(prefix = Constants.CONFIGURATION_PREFIX
			+ ".xss-filter", name = "enable", matchIfMissing = false)
	public static class XssFilterConfiguration {

		@Bean
		@ConditionalOnMissingBean
		XssHandler getXssHandler() {
			return new DefaultXssHandler();
		}

		@Bean
		FilterRegistrationBean<XssFilter> getXssFilterRegistrationBean(XssHandler xssHandler,
				CommonsServerProperties commonsServerProperties) {
			FilterRegistrationBean<XssFilter> filterRegistrationBean = new FilterRegistrationBean<>();
			filterRegistrationBean.setFilter(new XssFilter(xssHandler));
			filterRegistrationBean.setOrder(Constants.XSS_FILTER_ORDER);
			filterRegistrationBean.setEnabled(true);
			FilterProperties filterProperties = commonsServerProperties.getXssFilter();
			if (null != filterProperties && CollectionUtils.isNotEmpty(filterProperties.getPathPatterns())) {
				filterRegistrationBean.setUrlPatterns(filterProperties.getPathPatterns());
			} else {
				filterRegistrationBean.addUrlPatterns("/*");
			}
			return filterRegistrationBean;
		}
	}

	@Bean
	public RequestResourceAspect getRequestResourceAspect() {
		return new RequestResourceAspect();
	}
}
