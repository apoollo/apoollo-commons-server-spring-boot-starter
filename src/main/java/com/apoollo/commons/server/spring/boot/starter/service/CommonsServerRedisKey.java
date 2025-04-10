/**
 * 
 */
package com.apoollo.commons.server.spring.boot.starter.service;

import com.apoollo.commons.util.LangUtils;
import com.apoollo.commons.util.redis.service.RedisNameSpaceKey;

/**
 * @author liuyulong
 */
public interface CommonsServerRedisKey {

	public RedisNameSpaceKey getRedisNameSpaceKey();

	public default String getCommonsServerKey(String... services) {
		return getRedisNameSpaceKey().getKey("commons-server", LangUtils.getStream(services));
	}

	public default String getCommonResourceKey() {
		return getCommonsServerKey("resource");
	}

	public default String getCommonsUserKey(String accesskey) {
		return getCommonsServerKey("user", accesskey);
	}

	public default String getCommonsAuthorizationKey(String accesskey) {
		return getCommonsServerKey("authorization", accesskey);
	}

	public default String getCountKey(String accesskey, String resourcePin, String suffix) {
		return getCommonsServerKey("counter", accesskey, resourcePin, suffix);
	}

	public default String getSyncKey(String key) {
		return getCommonsServerKey("sync", key);
	}
}
