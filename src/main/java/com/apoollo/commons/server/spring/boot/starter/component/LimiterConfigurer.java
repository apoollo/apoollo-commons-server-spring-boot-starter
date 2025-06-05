/**
 * 
 */
package com.apoollo.commons.server.spring.boot.starter.component;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;

import com.apoollo.commons.server.spring.boot.starter.limiter.CorsLimiter;
import com.apoollo.commons.server.spring.boot.starter.limiter.DailyCountLimiter;
import com.apoollo.commons.server.spring.boot.starter.limiter.FlowLimiter;
import com.apoollo.commons.server.spring.boot.starter.limiter.IpLimiter;
import com.apoollo.commons.server.spring.boot.starter.limiter.NonceLimiter;
import com.apoollo.commons.server.spring.boot.starter.limiter.RefererLimiter;
import com.apoollo.commons.server.spring.boot.starter.limiter.SignatureLimiter;
import com.apoollo.commons.server.spring.boot.starter.limiter.SyncLimiter;
import com.apoollo.commons.server.spring.boot.starter.limiter.core.DefaultCorsLimiter;
import com.apoollo.commons.server.spring.boot.starter.limiter.core.DefaultDailyCountLimiter;
import com.apoollo.commons.server.spring.boot.starter.limiter.core.DefaultFlowLimiter;
import com.apoollo.commons.server.spring.boot.starter.limiter.core.DefaultIpLimiter;
import com.apoollo.commons.server.spring.boot.starter.limiter.core.DefaultNonceLimiter;
import com.apoollo.commons.server.spring.boot.starter.limiter.core.DefaultRefererLimiter;
import com.apoollo.commons.server.spring.boot.starter.limiter.core.DefaultSignatureLimiter;
import com.apoollo.commons.server.spring.boot.starter.limiter.core.DefaultSyncLimiter;
import com.apoollo.commons.server.spring.boot.starter.service.CommonsServerRedisKey;
import com.apoollo.commons.util.redis.service.CountLimiter;
import com.apoollo.commons.util.redis.service.RedisNameSpaceKey;
import com.apoollo.commons.util.redis.service.SlidingWindowLimiter;
import com.apoollo.commons.util.request.context.NonceValidator;
import com.apoollo.commons.util.request.context.def.StrictNonceValidaor;

/**
 * liuyulong
 */
@Configuration
public class LimiterConfigurer {

	@Bean
	@ConditionalOnMissingBean
	NonceValidator getNonceValidator(RedisNameSpaceKey redisNameSpaceKey, StringRedisTemplate redisTemplate) {
		return new StrictNonceValidaor(redisNameSpaceKey, redisTemplate);
	}

	@Bean
	@ConditionalOnMissingBean
	NonceLimiter getNonceLimiter(NonceValidator nonceValidator) {
		return new DefaultNonceLimiter(nonceValidator);
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
	SyncLimiter getSyncLimiter(RedisTemplate<String, String> redisTemplate,
			CommonsServerRedisKey commonsServerRedisKey) {
		return new DefaultSyncLimiter(redisTemplate, commonsServerRedisKey);
	}
	
	@Bean
	@ConditionalOnMissingBean
	FlowLimiter getFlowLimiter(SlidingWindowLimiter slidingWindowLimiter) {
		return new DefaultFlowLimiter(slidingWindowLimiter);
	}
	
	@Bean
	@ConditionalOnMissingBean
	DailyCountLimiter getDailyCountLimiter(CountLimiter countLimiter) {
		return new DefaultDailyCountLimiter(countLimiter);
	}
}
