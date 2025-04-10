/**
 * 
 */
package com.apoollo.commons.server.spring.boot.starter.service.impl;

import java.time.Duration;

import org.springframework.data.redis.core.RedisTemplate;

import com.apoollo.commons.server.spring.boot.starter.service.CommonsServerRedisKey;
import com.apoollo.commons.server.spring.boot.starter.service.SyncService;
import com.apoollo.commons.util.redis.RedisUtils;

/**
 * @author liuyulong
 * @since 2024-11-29
 */
public class DefaultSyncService implements SyncService {

	private RedisTemplate<String, String> redisTemplate;
	private CommonsServerRedisKey commonsServerRedisKey;

	public DefaultSyncService(RedisTemplate<String, String> redisTemplate,
			CommonsServerRedisKey commonsServerRedisKey) {
		super();
		this.redisTemplate = redisTemplate;
		this.commonsServerRedisKey = commonsServerRedisKey;
	}

	@Override
	public boolean lock(String key, Duration duration) {
		return RedisUtils.fight(redisTemplate, commonsServerRedisKey.getSyncKey(key), duration);
	}

	@Override
	public void unlock(String key) {
		redisTemplate.delete(commonsServerRedisKey.getSyncKey(key));
	}

}
