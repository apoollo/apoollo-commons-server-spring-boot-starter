/**
 * 
 */
package com.apoollo.commons.server.spring.boot.starter.service.impl;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

import com.alibaba.fastjson2.JSON;
import com.apoollo.commons.server.spring.boot.starter.service.UserManager;
import com.apoollo.commons.util.JwtUtils.Renewal;
import com.apoollo.commons.util.LangUtils;
import com.apoollo.commons.util.redis.service.RedisNameSpaceKey;
import com.apoollo.commons.util.request.context.User;
import com.apoollo.commons.util.request.context.core.DefaultUser;
import com.apoollo.commons.util.request.context.core.DefaultUser.SerializableUser;
import com.apoollo.commons.util.web.spring.Instance;

/**
 * @author liuyulong
 * @since 2023年8月30日
 */
public class DefaultUserManager implements UserManager {

	private static final Logger LOGGER = LoggerFactory.getLogger(DefaultUserManager.class);

	// private CacheManagerService cacheManagerService;
	private Instance instance;
	private StringRedisTemplate redisTemplate;
	private RedisNameSpaceKey redisNameSpaceKey;

	private List<? extends User> users;

	public DefaultUserManager(Instance instance, StringRedisTemplate redisTemplate, RedisNameSpaceKey redisNameSpaceKey,
			List<? extends User> users) {
		super();
		this.instance = instance;
		// this.cacheManagerService = cacheManagerService;
		this.redisTemplate = redisTemplate;
		this.redisNameSpaceKey = redisNameSpaceKey;
		this.users = users;
	}

	// private void doCache(Consumer<Cache> consumer) {
	// cacheManagerService.doCache(UserManager.CACHE_NAME, consumer);
	// }

	protected User getUserFromRedis(String key) {
		String userJsonString = redisTemplate.opsForValue().get(key);
		SerializableUser serializableUser = LangUtils.parseObject(userJsonString, SerializableUser.class);
		return DefaultUser.toUser(instance, serializableUser);
	}

	protected User getUserFromConfig(String accessKey) {
		return LangUtils.getStream(users)//
				.filter(user -> StringUtils.equals(accessKey, user.getAccessKey()))//
				.findFirst()//
				.orElse(null);
	}

	protected String getUserRedisKey(String accessKey) {
		return redisNameSpaceKey.getKey(RedisNameSpaceKey.USER, accessKey);
	}

	@Override
	// @Cacheable(value = UserManager.CACHE_NAME, sync = true)
	public User getUser(String accessKey) {
		long startTimestamp = System.currentTimeMillis();
		User user = getUserFromConfig(accessKey);
		if (null == user) {
			user = getUserFromRedis(getUserRedisKey(accessKey));
		}
		long endTimestamp = System.currentTimeMillis();
		LOGGER.info("getUser elapsedTime：" + (endTimestamp - startTimestamp) + "ms");
		return user;
	}

	@Override
	public void setUser(User user, Long timeout, TimeUnit timeUnit) {
		String key = getUserRedisKey(user.getAccessKey());
		String value = JSON.toJSONString(DefaultUser.toSerializableUser(user));
		if (null != timeout && null != timeUnit && 0 != timeout) {
			redisTemplate.opsForValue().set(key, value, timeout, timeUnit);
		} else {
			redisTemplate.opsForValue().set(key, value);
		}
		// doCache(cache -> cache.put(user.getAccessKey(), user));
	}

	@Override
	public void delete(String accessKey) {
		String key = getUserRedisKey(accessKey);
		redisTemplate.delete(key);
		// doCache(cache -> cache.evict(accessKey));
	}

	@Override
	public void renewal(String accessKey, Renewal renewal) {
		String key = getUserRedisKey(accessKey);
		redisTemplate.expireAt(key, renewal.getRenewalExpiresAt());
	}

	public void refresh(String accessKey, Boolean enable, String secretKey) {
		Validate.isTrue(null != enable || null != secretKey, "enable or secret must not be null of one");
		String key = getUserRedisKey(accessKey);
		User user = getUserFromRedis(key);
		if (null != user) {
			DefaultUser defaultUser = (DefaultUser) user;
			if (null != enable) {
				defaultUser.setEnable(enable);
			}
			if (StringUtils.isNotBlank(secretKey)) {
				defaultUser.setSecretKey(secretKey);
			}
			setUser(defaultUser, redisTemplate.getExpire(key, TimeUnit.SECONDS), TimeUnit.SECONDS);
		}
	}

	@Override
	public void refresh(String accessKey, Boolean enable) {
		refresh(accessKey, enable, null);

	}

	@Override
	public void refresh(String accessKey, String secretKey) {
		refresh(accessKey, null, secretKey);
	}

}
