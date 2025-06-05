/**
 * 
 */
package com.apoollo.commons.server.spring.boot.starter.limiter;

import java.time.Duration;

/**
 * @author liuyulong
 * @since 2024-11-29
 */
public interface SyncLimiter {

	public default void limit(String resourcePin, Duration duration) {
		limit(null, resourcePin, duration);
	}

	public default void unlimit(String resourcePin) {
		unlimit(null, resourcePin);
	}

	public void limit(String accessKey, String resourcePin, Duration duration);

	public void unlimit(String accessKey, String resourcePin);
}
