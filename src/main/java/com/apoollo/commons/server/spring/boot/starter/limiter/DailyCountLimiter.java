/**
 * 
 */
package com.apoollo.commons.server.spring.boot.starter.limiter;

import com.apoollo.commons.util.redis.service.impl.CommonsCountLimiter.Incremented;

/**
 * @author liuyulong
 * @since 2025-06-05
 */
public interface DailyCountLimiter {

	public default Incremented limit(String resourcePin, Long limitCount) {
		return limit(null, resourcePin, limitCount);
	}

	public Incremented limit(String accessKey, String resourcePin, Long limitCount);
}
