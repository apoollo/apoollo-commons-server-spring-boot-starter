/**
 * 
 */
package com.apoollo.commons.server.spring.boot.starter.service.impl;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;

import com.apoollo.commons.server.spring.boot.starter.service.FlowLimiter;
import com.apoollo.commons.util.exception.AppServerOverloadedException;
import com.apoollo.commons.util.redis.service.SlidingWindowLimiter;

/**
 * @author liuyulong
 */
public class SlidingWindowLimiterImpl implements FlowLimiter {

	private SlidingWindowLimiter slidingWindowLimiter;

	public SlidingWindowLimiterImpl(SlidingWindowLimiter slidingWindowLimiter) {
		this.slidingWindowLimiter = slidingWindowLimiter;
	}

	@Override
	public void tryAccess(String accessKey, String resourcePin, Long limitCount) {
		if (null != limitCount && limitCount > 0) {

			if (limitCount % 2 != 0) {
				throw new IllegalArgumentException("limitCount illegal: " + limitCount);
			}

			String key = Stream.of(accessKey, resourcePin).filter(StringUtils::isNotBlank)
					.collect(Collectors.joining(":"));

			// 500毫秒内可以调用的次数
			if (!slidingWindowLimiter.access(key, 500, limitCount / 2)) {
				throw new AppServerOverloadedException("流量超限");
			}
		}
	}

}
