/**
 * 
 */
package com.apoollo.commons.server.spring.boot.starter;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;

import com.apoollo.commons.server.spring.boot.starter.component.aspect.RequestResourceAspect;
import com.apoollo.commons.server.spring.boot.starter.component.bodyadvice.ResponseBodyContextAdvice;
import com.apoollo.commons.util.redis.service.CountLimiter;
import com.apoollo.commons.util.redis.service.RedisNameSpaceKey;
import com.apoollo.commons.util.redis.service.SlidingWindowLimiter;
import com.apoollo.commons.util.redis.service.impl.CommonsCountLimiter;
import com.apoollo.commons.util.redis.service.impl.CommonsSlidingWindowLimiter;
import com.apoollo.commons.util.request.context.ContentEscapeMethod;
import com.apoollo.commons.util.request.context.Instances;
import com.apoollo.commons.util.request.context.access.User;
import com.apoollo.commons.util.request.context.access.UserRequestResourceMatcher;
import com.apoollo.commons.util.request.context.core.DefaultCapacitySupport;
import com.apoollo.commons.util.request.context.core.DefaultCapacitySupport.SerializebleCapacitySupport;
import com.apoollo.commons.util.request.context.core.DefaultContentEscapeXss;
import com.apoollo.commons.util.request.context.limiter.ContentEscapeHandler;
import com.apoollo.commons.util.request.context.limiter.CorsLimiter;
import com.apoollo.commons.util.request.context.limiter.FlowLimiter;
import com.apoollo.commons.util.request.context.limiter.IpLimiter;
import com.apoollo.commons.util.request.context.limiter.Limiters;
import com.apoollo.commons.util.request.context.limiter.NonceLimiter;
import com.apoollo.commons.util.request.context.limiter.NonceValidator;
import com.apoollo.commons.util.request.context.limiter.RefererLimiter;
import com.apoollo.commons.util.request.context.limiter.SignatureLimiter;
import com.apoollo.commons.util.request.context.limiter.SyncLimiter;
import com.apoollo.commons.util.request.context.limiter.TimeUnitPatternCountLimiter;
import com.apoollo.commons.util.request.context.limiter.WrapResponseHandler;
import com.apoollo.commons.util.request.context.limiter.core.DefaultContentEscapeHandler;
import com.apoollo.commons.util.request.context.limiter.core.DefaultCorsLimiter;
import com.apoollo.commons.util.request.context.limiter.core.DefaultFlowLimiter;
import com.apoollo.commons.util.request.context.limiter.core.DefaultIpLimiter;
import com.apoollo.commons.util.request.context.limiter.core.DefaultLimiters;
import com.apoollo.commons.util.request.context.limiter.core.DefaultNonceLimiter;
import com.apoollo.commons.util.request.context.limiter.core.DefaultRefererLimiter;
import com.apoollo.commons.util.request.context.limiter.core.DefaultSignatureLimiter;
import com.apoollo.commons.util.request.context.limiter.core.DefaultSyncLimiter;
import com.apoollo.commons.util.request.context.limiter.core.DefaultTimeUnitPatternCountLimiter;
import com.apoollo.commons.util.request.context.limiter.core.DefaultWrapResponseHandler;
import com.apoollo.commons.util.request.context.limiter.core.StrictNonceValidaor;
import com.apoollo.commons.util.request.context.limiter.core.UseLimiters;
import com.apoollo.commons.util.request.context.limiter.support.LimitersSupport;
import com.apoollo.commons.util.request.context.model.RequestContextCapacitySupport;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * liuyulong
 */
@AutoConfiguration
@ConditionalOnWebApplication
public class CapacityConfigurer {

	@Bean
	@ConditionalOnMissingBean
	ContentEscapeHandler getContentEscapeHandler(RequestContextCapacitySupport requestContextCapacitySupport) {
		return new DefaultContentEscapeHandler(requestContextCapacitySupport);
	}

	@Bean
	@ConditionalOnMissingBean
	SlidingWindowLimiter getSlidingWindowLimiter(RedisTemplate<String, String> redisTemplate,
			RedisNameSpaceKey redisNameSpaceKey) {
		return new CommonsSlidingWindowLimiter(redisTemplate, redisNameSpaceKey);
	}

	@Bean
	@ConditionalOnMissingBean
	CountLimiter getCountLimiter(StringRedisTemplate stringRedisTemplate, RedisNameSpaceKey redisNameSpaceKey) {
		return new CommonsCountLimiter(stringRedisTemplate, redisNameSpaceKey);
	}

	@Bean
	@ConditionalOnMissingBean
	NonceLimiter getNonceLimiter(Instances instances) {
		return new DefaultNonceLimiter(instances.getNonceValidator(StrictNonceValidaor.class));
	}

	@Bean
	@ConditionalOnMissingBean
	SignatureLimiter getSignatureLimiter() {
		return new DefaultSignatureLimiter();
	}

	@Bean
	@ConditionalOnMissingBean
	CorsLimiter getCorsLimiter() {
		return new DefaultCorsLimiter();
	}

	@Bean
	@ConditionalOnMissingBean
	IpLimiter getIpLimiter() {
		return new DefaultIpLimiter();
	}

	@Bean
	@ConditionalOnMissingBean
	RefererLimiter getRefererLimiter() {
		return new DefaultRefererLimiter();
	}

	@Bean
	@ConditionalOnMissingBean
	SyncLimiter getSyncLimiter(RedisTemplate<String, String> redisTemplate, RedisNameSpaceKey redisNameSpaceKey) {
		return new DefaultSyncLimiter(redisTemplate, redisNameSpaceKey);
	}

	@Bean
	@ConditionalOnMissingBean
	FlowLimiter getFlowLimiter(SlidingWindowLimiter slidingWindowLimiter) {
		return new DefaultFlowLimiter(slidingWindowLimiter);
	}

	@Bean
	@ConditionalOnMissingBean
	TimeUnitPatternCountLimiter getTimeUnitPatternCountLimiter(CountLimiter countLimiter) {
		return new DefaultTimeUnitPatternCountLimiter(countLimiter);
	}

	@Bean
	@ConditionalOnMissingBean
	SerializebleCapacitySupport getSerializebleCapacitySupport() {
		SerializebleCapacitySupport capacitySupport = new SerializebleCapacitySupport();
		capacitySupport.setEnableCapacity(true);
		capacitySupport.setEnableResponseWrapper(true);
		capacitySupport.setWrapResponseHandlerClass(DefaultWrapResponseHandler.class);
		capacitySupport.setResourcePin("platform");
		return capacitySupport;
	}

	@Bean
	@ConditionalOnMissingBean
	ResponseBodyContextAdvice getResponseContextBodyAdvice(RequestContextCapacitySupport requestContextSupport) {
		return new ResponseBodyContextAdvice(requestContextSupport);
	}

	@Bean
	@ConditionalOnMissingBean
	RequestResourceAspect getRequestResourceAspect() {
		return new RequestResourceAspect();
	}

	@Bean
	Limiters<LimitersSupport> getSupportLimiters(NonceLimiter nonceLimiter, SignatureLimiter signatureLimter,
			CorsLimiter corsLimiter, IpLimiter ipLimiter, RefererLimiter refererLimiter, SyncLimiter syncLimiter,
			FlowLimiter flowLimiter, TimeUnitPatternCountLimiter dailyCountLimiter) {
		return new DefaultLimiters<>(nonceLimiter, signatureLimter, corsLimiter, ipLimiter, refererLimiter, syncLimiter,
				flowLimiter, dailyCountLimiter);
	}

	@Bean
	Limiters<User> getUseLimiters(NonceLimiter nonceLimiter, SignatureLimiter signatureLimter, CorsLimiter corsLimiter,
			IpLimiter ipLimiter, RefererLimiter refererLimiter, SyncLimiter syncLimiter, FlowLimiter flowLimiter,
			TimeUnitPatternCountLimiter dailyCountLimiter, UserRequestResourceMatcher requestResourceMatcher) {
		return new UseLimiters(nonceLimiter, signatureLimter, corsLimiter, ipLimiter, refererLimiter, syncLimiter,
				flowLimiter, dailyCountLimiter, requestResourceMatcher);
	}

	@Bean
	RequestContextCapacitySupport getRequestContextSupport(Instances instances,
			SerializebleCapacitySupport serializebleCapacitySupport) {
		return new RequestContextCapacitySupport(instances.getContentEscapeMethod(DefaultContentEscapeXss.class),
				instances.getWrapResponseHandler(DefaultWrapResponseHandler.class),
				DefaultCapacitySupport.toCapacitySupport(instances, serializebleCapacitySupport));
	}

	@Bean
	NonceValidator getNonceValidator(StringRedisTemplate redisTemplate, RedisNameSpaceKey redisNameSpaceKey) {
		return new StrictNonceValidaor(redisTemplate, redisNameSpaceKey);
	}

	@Bean
	WrapResponseHandler getWrapResponseHandler(ObjectMapper objectMapper) {
		return new DefaultWrapResponseHandler(objectMapper);
	}

	@Bean
	ContentEscapeMethod getContentEscapeMethod() {
		return new DefaultContentEscapeXss();
	}

}
