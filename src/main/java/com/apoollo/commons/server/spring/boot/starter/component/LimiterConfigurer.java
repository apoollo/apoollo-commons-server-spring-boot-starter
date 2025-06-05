/**
 * 
 */
package com.apoollo.commons.server.spring.boot.starter.component;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

import com.apoollo.commons.server.spring.boot.starter.limiter.NonceLimiter;
import com.apoollo.commons.server.spring.boot.starter.limiter.SignatureLimiter;
import com.apoollo.commons.server.spring.boot.starter.limiter.core.DefaultNonceLimiter;
import com.apoollo.commons.server.spring.boot.starter.limiter.core.DefaultSignatureLimiter;
import com.apoollo.commons.util.redis.service.RedisNameSpaceKey;
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
}
