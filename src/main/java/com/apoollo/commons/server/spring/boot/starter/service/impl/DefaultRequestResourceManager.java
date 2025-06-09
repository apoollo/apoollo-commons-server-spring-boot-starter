/**
 * 
 */
package com.apoollo.commons.server.spring.boot.starter.service.impl;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.StringRedisTemplate;

import com.alibaba.fastjson2.JSON;
import com.apoollo.commons.server.spring.boot.starter.properties.CommonsServerProperties;
import com.apoollo.commons.server.spring.boot.starter.service.RequestResourceManager;
import com.apoollo.commons.util.LangUtils;
import com.apoollo.commons.util.redis.service.RedisNameSpaceKey;
import com.apoollo.commons.util.request.context.access.RequestResource;
import com.apoollo.commons.util.request.context.access.core.DefaultRequestResource;
import com.apoollo.commons.util.request.context.access.core.DefaultRequestResource.SerializableRequestResource;
import com.apoollo.commons.util.web.spring.Instance;

/**
 * @author liuyulong
 * @since 2023年9月28日
 */
public class DefaultRequestResourceManager implements RequestResourceManager {

	private static final Logger LOGGER = LoggerFactory.getLogger(DefaultRequestResourceManager.class);

	// private CacheManagerService cacheManagerService;
	private Instance instance;
	private StringRedisTemplate redisTemplate;
	private RedisNameSpaceKey redisNameSpaceKey;
	private List<? extends RequestResource> requestResources;

	public DefaultRequestResourceManager(Instance instance, StringRedisTemplate redisTemplate,
			RedisNameSpaceKey redisNameSpaceKey, CommonsServerProperties commonsServerProperties) {
		super();
		// this.cacheManagerService = cacheManagerService;
		this.instance = instance;
		this.redisTemplate = redisTemplate;
		this.redisNameSpaceKey = redisNameSpaceKey;
		this.requestResources = commonsServerProperties.getRbac().getRequestResources();
	}

	// private void doCache(Consumer<Cache> consumer) {
	// cacheManagerService.doCache(RequestResourceManager.CACHE_NAME, consumer);
	// }

	@Override
	@Cacheable(value = RequestResourceManager.CACHE_NAME, key = "#requestMappingPath", sync = true)
	public RequestResource getRequestResource(String requestMappingPath) {
		long startTimestamp = System.currentTimeMillis();
		RequestResource requestResource = getRequestResourceFromConfig(requestMappingPath);
		if (null == requestResource) {
			requestResource = getRequestResourceFromRedis(requestMappingPath);
		}
		long endTimestamp = System.currentTimeMillis();
		LOGGER.info("getRequestResource elapsedTime：" + (endTimestamp - startTimestamp) + "ms");
		return requestResource;
	}

	public RequestResource getRequestResourceFromConfig(String requestMappingPath) {
		return LangUtils.getStream(requestResources)//
				.filter(resource -> StringUtils.equals(resource.getRequestMappingPath(), requestMappingPath))//
				.findFirst()//
				.orElse(null);
	}

	public RequestResource getRequestResourceFromRedis(String requestMappingPath) {
		String key = getRequestResourceRedisKey();
		Object object = redisTemplate.opsForHash().get(key, requestMappingPath);
		SerializableRequestResource serializableRequestResource = JSON.parseObject((String) object,
				SerializableRequestResource.class);
		RequestResource requestResource = DefaultRequestResource.toRequestResource(instance,
				serializableRequestResource);
		return requestResource;
	}

	public String getRequestResourceRedisKey() {
		return redisNameSpaceKey.getKey(RedisNameSpaceKey.REQUEST_RESOURCE);
	}

	@Override
	public void setRequestResource(RequestResource requestResource) {
		redisTemplate.opsForHash().put(getRequestResourceRedisKey(), requestResource.getRequestMappingPath(),
				JSON.toJSONString(DefaultRequestResource.toSerializableRequestResource(requestResource)));
		// doCache(cache -> cache.put(requestResource.getRequestMappingPath(),
		// requestResource));
	}

	@Override
	public void deleteRequestResource(String requestMappingPath) {
		redisTemplate.opsForHash().delete(getRequestResourceRedisKey(), requestMappingPath);
		// doCache(cache -> cache.evict(requestMappingPath));
	}

}
