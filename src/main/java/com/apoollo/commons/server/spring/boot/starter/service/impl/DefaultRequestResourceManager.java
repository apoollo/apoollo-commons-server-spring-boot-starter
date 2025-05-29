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
import com.apoollo.commons.server.spring.boot.starter.service.CommonsServerRedisKey;
import com.apoollo.commons.server.spring.boot.starter.service.Instance;
import com.apoollo.commons.server.spring.boot.starter.service.RequestResourceManager;
import com.apoollo.commons.util.LangUtils;
import com.apoollo.commons.util.request.context.RequestResource;
import com.apoollo.commons.util.request.context.def.AccessStrategy;
import com.apoollo.commons.util.request.context.def.DefaultRequestResource;

import lombok.Getter;
import lombok.Setter;

/**
 * @author liuyulong
 * @since 2023年9月28日
 */
public class DefaultRequestResourceManager implements RequestResourceManager {

	private static final Logger LOGGER = LoggerFactory.getLogger(DefaultRequestResourceManager.class);

	// private CacheManagerService cacheManagerService;
	private Instance instance;
	private StringRedisTemplate redisTemplate;
	private CommonsServerRedisKey commonsServerRedisKey;
	private List<? extends RequestResource> requestResources;

	public DefaultRequestResourceManager(Instance instance, StringRedisTemplate redisTemplate,
			CommonsServerRedisKey commonsServerRedisKey, CommonsServerProperties commonsServerProperties) {
		super();
		// this.cacheManagerService = cacheManagerService;
		this.instance = instance;
		this.redisTemplate = redisTemplate;
		this.commonsServerRedisKey = commonsServerRedisKey;
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
		RequestResource requestResource = toRequestResource(object);
		return requestResource;
	}

	public String getRequestResourceRedisKey() {
		return commonsServerRedisKey.getCommonResourceKey();
	}

	@Override
	public void setRequestResource(RequestResource requestResource) {
		redisTemplate.opsForHash().put(getRequestResourceRedisKey(), requestResource.getRequestMappingPath(),
				JSON.toJSONString(toSerializableRequestResource(requestResource)));
		// doCache(cache -> cache.put(requestResource.getRequestMappingPath(),
		// requestResource));
	}

	@Override
	public void deleteRequestResource(String requestMappingPath) {
		redisTemplate.opsForHash().delete(getRequestResourceRedisKey(), requestMappingPath);
		// doCache(cache -> cache.evict(requestMappingPath));
	}

	public RequestResource toRequestResource(Object object) {
		SerializableRequestResource serializableRequestResource = LangUtils.parseObject(object,
				SerializableRequestResource.class);
		DefaultRequestResource defaultRequestResource = new DefaultRequestResource();
		defaultRequestResource.setAccessStrategy(serializableRequestResource.getAccessStrategy());
		defaultRequestResource.setEnable(serializableRequestResource.getEnable());
		defaultRequestResource.setEnableContentEscape(serializableRequestResource.getEnableContentEscape());
		defaultRequestResource.setEnableResponseWrapper(serializableRequestResource.getEnableResponseWrapper());
		defaultRequestResource.setEnableSignature(serializableRequestResource.getEnableSignature());
		defaultRequestResource.setEnableSync(serializableRequestResource.getEnableSync());
		defaultRequestResource.setLimtPlatformQps(serializableRequestResource.getLimtPlatformQps());
		defaultRequestResource.setLimtUserQps(serializableRequestResource.getLimtUserQps());
		defaultRequestResource.setName(serializableRequestResource.getName());
		defaultRequestResource.setRequestMappingPath(serializableRequestResource.getRequestMappingPath());
		defaultRequestResource.setResourcePin(serializableRequestResource.getResourcePin());
		defaultRequestResource.setRoles(serializableRequestResource.getRoles());
		defaultRequestResource
				.setSignatureExcludeHeaderNames(serializableRequestResource.getSignatureExcludeHeaderNames());
		defaultRequestResource
				.setSignatureIncludeHeaderNames(serializableRequestResource.getSignatureIncludeHeaderNames());
		defaultRequestResource.setSignatureSecret(serializableRequestResource.getSignatureSecret());
		if (null != serializableRequestResource.contentEscapeMethodClass) {
			defaultRequestResource
					.setContentEscapeMethod(instance.getInstance(serializableRequestResource.contentEscapeMethodClass));
		}
		if (null != serializableRequestResource.wrapResponseHandlerClass) {
			defaultRequestResource
					.setWrapResponseHandler(instance.getInstance(serializableRequestResource.wrapResponseHandlerClass));
		}
		return defaultRequestResource;
	}

	public SerializableRequestResource toSerializableRequestResource(RequestResource requestResource) {
		SerializableRequestResource serializableRequestResource = new SerializableRequestResource();
		serializableRequestResource.setAccessStrategy(requestResource.getAccessStrategy());
		serializableRequestResource.setEnable(requestResource.getEnable());
		serializableRequestResource.setEnableContentEscape(requestResource.getEnableContentEscape());
		serializableRequestResource.setEnableResponseWrapper(requestResource.getEnableResponseWrapper());
		serializableRequestResource.setEnableSignature(requestResource.getEnableSignature());
		serializableRequestResource.setEnableSync(requestResource.getEnableSync());
		serializableRequestResource.setLimtPlatformQps(requestResource.getLimtPlatformQps());
		serializableRequestResource.setLimtUserQps(requestResource.getLimtUserQps());
		serializableRequestResource.setName(requestResource.getName());
		serializableRequestResource.setRequestMappingPath(requestResource.getRequestMappingPath());
		serializableRequestResource.setResourcePin(requestResource.getResourcePin());
		serializableRequestResource.setRoles(requestResource.getRoles());
		serializableRequestResource.setSignatureExcludeHeaderNames(requestResource.getSignatureExcludeHeaderNames());
		serializableRequestResource.setSignatureIncludeHeaderNames(requestResource.getSignatureIncludeHeaderNames());
		serializableRequestResource.setSignatureSecret(requestResource.getSignatureSecret());
		if (null != requestResource.getContentEscapeMethod()) {
			serializableRequestResource
					.setContentEscapeMethodClass(requestResource.getContentEscapeMethod().getClass().toString());
		}
		if (null != requestResource.getWrapResponseHandler()) {
			serializableRequestResource
					.setWrapResponseHandlerClass(requestResource.getWrapResponseHandler().getClass().toString());
		}
		return serializableRequestResource;
	}

	@Getter
	@Setter
	public static class SerializableRequestResource {

		private Boolean enable;
		private String resourcePin;
		private String name;
		private String requestMappingPath;
		private AccessStrategy accessStrategy;
		private Long limtUserQps;
		private Long limtPlatformQps;
		private String[] roles;
		private Boolean enableSync;
		private Boolean enableSignature;
		private String signatureSecret;
		private List<String> signatureExcludeHeaderNames;
		private List<String> signatureIncludeHeaderNames;
		private Boolean enableContentEscape;
		private String contentEscapeMethodClass;
		private Boolean enableResponseWrapper;
		private String wrapResponseHandlerClass;

	}

}
