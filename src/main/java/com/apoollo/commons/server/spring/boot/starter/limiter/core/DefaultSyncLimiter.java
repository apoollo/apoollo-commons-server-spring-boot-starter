/**
 * 
 */
package com.apoollo.commons.server.spring.boot.starter.limiter.core;

import java.time.Duration;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;

import com.apoollo.commons.server.spring.boot.starter.limiter.SyncLimiter;
import com.apoollo.commons.server.spring.boot.starter.service.CommonsServerRedisKey;
import com.apoollo.commons.util.exception.AppServerOverloadedException;
import com.apoollo.commons.util.redis.RedisUtils;

/**
 * @author liuyulong
 * @since 2024-11-29
 */
public class DefaultSyncLimiter implements SyncLimiter {

	private RedisTemplate<String, String> redisTemplate;
	private CommonsServerRedisKey commonsServerRedisKey;

	public DefaultSyncLimiter(RedisTemplate<String, String> redisTemplate,
			CommonsServerRedisKey commonsServerRedisKey) {
		super();
		this.redisTemplate = redisTemplate;
		this.commonsServerRedisKey = commonsServerRedisKey;
	}

	@Override
	public void limit(String accessKey, String resourcePin, Duration duration) {
		String key = Stream.of(accessKey, resourcePin).filter(StringUtils::isNotBlank).collect(Collectors.joining(":"));
		if (!RedisUtils.fight(redisTemplate, commonsServerRedisKey.getSyncKey(key), duration)) {
			throw new AppServerOverloadedException("当前操作只能同步执行");
		}
	}

	@Override
	public void unlimit(String accessKey, String resourcePin) {
		String key = Stream.of(accessKey, resourcePin).filter(StringUtils::isNotBlank).collect(Collectors.joining(":"));
		redisTemplate.delete(commonsServerRedisKey.getSyncKey(key));
	}

}
