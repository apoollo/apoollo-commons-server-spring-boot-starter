/**
 * 
 */
package com.apoollo.commons.server.spring.boot.starter.service.impl;

import java.util.Map;
import java.util.Optional;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.AntPathMatcher;

import com.alibaba.fastjson2.JSON;
import com.apoollo.commons.server.spring.boot.starter.service.CommonsServerRedisKey;
import com.apoollo.commons.util.LangUtils;
import com.apoollo.commons.util.request.context.Authorization;
import com.apoollo.commons.util.request.context.RequestResource;
import com.apoollo.commons.util.request.context.User;
import com.apoollo.commons.util.request.context.core.DefaultRequestAccessParameter;
import com.apoollo.commons.util.request.context.model.Authorized;

/**
 * @author liuyulong
 * @since 2023年9月27日
 */
public class DefaultAuthorization implements Authorization<Object> {

	private static final Logger LOGGER = LoggerFactory.getLogger(DefaultAuthorization.class);

	private static final AntPathMatcher ANT_PATH_MATCHER = new AntPathMatcher();
	private StringRedisTemplate redisTemplate;
	private CommonsServerRedisKey commonsServerRedisKey;
	private Map<String, Map<String, DefaultRequestAccessParameter>> permissions;

	public DefaultAuthorization(StringRedisTemplate redisTemplate, CommonsServerRedisKey commonsServerRedisKey,
			Map<String, Map<String, DefaultRequestAccessParameter>> permissions) {
		super();
		this.redisTemplate = redisTemplate;
		this.commonsServerRedisKey = commonsServerRedisKey;
		this.permissions = permissions;
	}

	public String getAuthorizedRedisKey(String accessKey) {
		return commonsServerRedisKey.getCommonsAuthorizationKey(accessKey);
	}

	@Override
	// @Cacheable(value = "Authorized", key =
	// "#user.getAccessKey()+':'+#requestResource.getResourcePin()")
	public Authorized<Object> getAuthorized(User user, RequestResource requestResource) {
		String accessKey = user.getAccessKey();
		String resourcePin = requestResource.getResourcePin();
		long startTimestamp = System.currentTimeMillis();
		Authorized<Object> authorized = null;

		// 本地配置文件设置权限判定授权
		if (MapUtils.isNotEmpty(permissions)) {
			Map<String, DefaultRequestAccessParameter> resources = permissions.get(accessKey);
			if (MapUtils.isNotEmpty(resources)) {
				DefaultRequestAccessParameter attachement = resources.get(resourcePin);
				if (null != attachement) {
					authorized = new Authorized<>(true, attachement);
					LOGGER.info("authorized by local permisson");
				}
			}
		}

		// 通过角色判定授权
		if (null == authorized && CollectionUtils.isNotEmpty(requestResource.getRoles())
				&& CollectionUtils.isNotEmpty(user.getRoles())) {

			Optional<String> roleOptional = requestResource.getRoles().stream()
					.filter(role -> user.getRoles().contains(role)).findAny();
			if (roleOptional.isPresent()) {
				authorized = new Authorized<>(true, null);
				LOGGER.info("authorized by role: " + roleOptional.get());
			}
		}

		// 通过URL匹配判定授权
		if (null == authorized && CollectionUtils.isNotEmpty(user.getAllowRequestAntPathPatterns())) {

			Optional<String> allowRequestAntPathPatternOptional = LangUtils
					.getStream(user.getAllowRequestAntPathPatterns())
					.filter(patter -> ANT_PATH_MATCHER.match(patter, requestResource.getRequestMappingPath()))
					.findAny();
			if (allowRequestAntPathPatternOptional.isPresent()) {
				authorized = new Authorized<>(true, null);
				LOGGER.info("authorized by allowRequestAntPathPattern: " + allowRequestAntPathPatternOptional.get());
			}
		}

		// 通过动态权限判定授权
		if (null == authorized) {
			String key = getAuthorizedRedisKey(accessKey);
			Object permission = redisTemplate.opsForHash().get(key, resourcePin);
			if (null != permission) {
				authorized = new Authorized<>(true,
						JSON.parseObject(permission.toString(), DefaultRequestAccessParameter.class));

				LOGGER.info("authorized by remote permission");
			}
		}

		long endTimestamp = System.currentTimeMillis();
		LOGGER.info("authorized elapsedTime：" + (endTimestamp - startTimestamp) + "ms");
		return authorized;
	}

	@Override
	public void setAuthorized(String accessKey, String resourcePin, Authorized<?> authorized) {
		String key = getAuthorizedRedisKey(accessKey);
		redisTemplate.opsForHash().put(key, resourcePin, JSON.toJSONString(authorized));
	}

	@Override
	public void deleteAuthorization(String accessKey, String resourcePin) {
		String key = getAuthorizedRedisKey(accessKey);
		redisTemplate.opsForHash().delete(key, resourcePin);
	}

}
