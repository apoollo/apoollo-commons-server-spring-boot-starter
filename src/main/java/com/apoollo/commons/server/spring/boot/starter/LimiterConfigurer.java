/**
 * 
 */
package com.apoollo.commons.server.spring.boot.starter;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;

import com.apoollo.commons.util.redis.service.CountLimiter;
import com.apoollo.commons.util.redis.service.RedisNameSpaceKey;
import com.apoollo.commons.util.redis.service.SlidingWindowLimiter;
import com.apoollo.commons.util.redis.service.impl.CommonsCountLimiter;
import com.apoollo.commons.util.redis.service.impl.CommonsSlidingWindowLimiter;
import com.apoollo.commons.util.request.context.CapacitySupport;
import com.apoollo.commons.util.request.context.EscapeMethod;
import com.apoollo.commons.util.request.context.NonceValidator;
import com.apoollo.commons.util.request.context.core.DefaultCapacitySupport;
import com.apoollo.commons.util.request.context.core.DefaultEscapeXss;
import com.apoollo.commons.util.request.context.core.StrictNonceValidaor;
import com.apoollo.commons.util.request.context.limiter.ContentEscapeHandler;
import com.apoollo.commons.util.request.context.limiter.CorsLimiter;
import com.apoollo.commons.util.request.context.limiter.DailyCountLimiter;
import com.apoollo.commons.util.request.context.limiter.FlowLimiter;
import com.apoollo.commons.util.request.context.limiter.IpLimiter;
import com.apoollo.commons.util.request.context.limiter.Limiters;
import com.apoollo.commons.util.request.context.limiter.NonceLimiter;
import com.apoollo.commons.util.request.context.limiter.RefererLimiter;
import com.apoollo.commons.util.request.context.limiter.SignatureLimiter;
import com.apoollo.commons.util.request.context.limiter.SyncLimiter;
import com.apoollo.commons.util.request.context.limiter.core.DefaultContentEscapeHandler;
import com.apoollo.commons.util.request.context.limiter.core.DefaultCorsLimiter;
import com.apoollo.commons.util.request.context.limiter.core.DefaultDailyCountLimiter;
import com.apoollo.commons.util.request.context.limiter.core.DefaultFlowLimiter;
import com.apoollo.commons.util.request.context.limiter.core.DefaultIpLimiter;
import com.apoollo.commons.util.request.context.limiter.core.DefaultLimiters;
import com.apoollo.commons.util.request.context.limiter.core.DefaultNonceLimiter;
import com.apoollo.commons.util.request.context.limiter.core.DefaultRefererLimiter;
import com.apoollo.commons.util.request.context.limiter.core.DefaultSignatureLimiter;
import com.apoollo.commons.util.request.context.limiter.core.DefaultSyncLimiter;
import com.apoollo.commons.util.request.context.limiter.support.LimitersSupport;

/**
 * liuyulong
 */
@AutoConfiguration
public class LimiterConfigurer {

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
	NonceValidator getNonceValidator(StringRedisTemplate redisTemplate, RedisNameSpaceKey redisNameSpaceKey) {
		return new StrictNonceValidaor(redisTemplate, redisNameSpaceKey);
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
	DailyCountLimiter getDailyCountLimiter(CountLimiter countLimiter) {
		return new DefaultDailyCountLimiter(countLimiter);
	}
	
	@Bean
	@ConditionalOnMissingBean
	CapacitySupport getCapacitySupport() {
		DefaultCapacitySupport capacitySupport= new DefaultCapacitySupport();
		capacitySupport.setResourcePin("platform");
		return capacitySupport;
	}

	@Bean
	@ConditionalOnMissingBean
	Limiters<LimitersSupport> getLimiters(NonceLimiter nonceLimiter, SignatureLimiter signatureLimter,
			CorsLimiter corsLimiter, IpLimiter ipLimiter, RefererLimiter refererLimiter, SyncLimiter syncLimiter,
			FlowLimiter flowLimiter, DailyCountLimiter dailyCountLimiter) {
		return new DefaultLimiters<>(nonceLimiter, signatureLimter, corsLimiter, ipLimiter, refererLimiter, syncLimiter,
				flowLimiter, dailyCountLimiter);
	}
}
