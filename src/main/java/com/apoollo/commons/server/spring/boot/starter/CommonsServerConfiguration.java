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
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.StringRedisTemplate;

import com.apoollo.commons.server.spring.boot.starter.component.ApplicationReady;
import com.apoollo.commons.server.spring.boot.starter.component.CommonsServerWebMvcConfigurer;
import com.apoollo.commons.server.spring.boot.starter.component.ComponentConfigurer;
import com.apoollo.commons.server.spring.boot.starter.component.LimiterConfigurer;
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
import com.apoollo.commons.server.spring.boot.starter.service.Instance;
import com.apoollo.commons.server.spring.boot.starter.service.LoggerWriter;
import com.apoollo.commons.server.spring.boot.starter.service.RequestResourceManager;
import com.apoollo.commons.server.spring.boot.starter.service.UserManager;
import com.apoollo.commons.server.spring.boot.starter.service.impl.DefaultAuthenticationJwtTokenDecoder;
import com.apoollo.commons.server.spring.boot.starter.service.impl.DefaultAuthorization;
import com.apoollo.commons.server.spring.boot.starter.service.impl.DefaultInstance;
import com.apoollo.commons.server.spring.boot.starter.service.impl.DefaultLoggerWriter;
import com.apoollo.commons.server.spring.boot.starter.service.impl.DefaultRequestContextDataBus;
import com.apoollo.commons.server.spring.boot.starter.service.impl.DefaultRequestResourceManager;
import com.apoollo.commons.server.spring.boot.starter.service.impl.DefaultUserManager;
import com.apoollo.commons.server.spring.boot.starter.service.impl.JwtTokenAccess;
import com.apoollo.commons.server.spring.boot.starter.service.impl.SecretKeyTokenAccess;
import com.apoollo.commons.util.JwtUtils.JwtToken;
import com.apoollo.commons.util.LangUtils;
import com.apoollo.commons.util.exception.AppServerOverloadedException;
import com.apoollo.commons.util.redis.service.CountLimiter;
import com.apoollo.commons.util.redis.service.RedisNameSpaceKey;
import com.apoollo.commons.util.request.context.Authorization;
import com.apoollo.commons.util.request.context.RequestContextDataBus;
import com.apoollo.commons.util.request.context.RequestContextInitail;
import com.apoollo.commons.util.request.context.limiter.FlowLimiter;
import com.apoollo.commons.util.web.captcha.CaptchaService;
import com.apoollo.commons.util.web.captcha.RedisCaptchaService;
import com.github.benmanes.caffeine.cache.Caffeine;

/**
 * @author liuyulong
 * @since 2023年8月22日
 */
@EnableCaching
@Configuration(proxyBeanMethods = true)
@ConditionalOnWebApplication
@ConditionalOnProperty(prefix = Constants.CONFIGURATION_PREFIX, name = "enable", matchIfMissing = true)
@Import({ CommonsServerWebMvcConfigurer.class, LimiterConfigurer.class, ComponentConfigurer.class,
		ApplicationReady.class, WelcomeController.class, ExceptionController.class, DynamicResourceController.class })
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
	Instance getInstance() {
		return new DefaultInstance();
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
	RequestResourceManager getRequestResourceManager(Instance instance, StringRedisTemplate redisTemplate,
			CommonsServerRedisKey commonsServerRedisKey, CommonsServerProperties commonsServerProperties) {
		return new DefaultRequestResourceManager(instance, redisTemplate, commonsServerRedisKey,
				commonsServerProperties);
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

}
