/**
 * 
 */
package com.apoollo.commons.server.spring.boot.starter.limiter.core;

import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;

import com.apoollo.commons.server.spring.boot.starter.limiter.DailyCountLimiter;
import com.apoollo.commons.util.exception.AppExceedingDailyMaximumUseTimesLimitException;
import com.apoollo.commons.util.redis.service.CountLimiter;
import com.apoollo.commons.util.redis.service.impl.CommonsCountLimiter.Incremented;

/**
 * @author liuyulong
 * @since 2025-06-05
 */
public class DefaultDailyCountLimiter implements DailyCountLimiter {

	private CountLimiter countLimiter;
	
	public DefaultDailyCountLimiter(CountLimiter countLimiter) {
		super();
		this.countLimiter = countLimiter;
	}

	@Override
	public Incremented limit(String accessKey, String resourcePin, Long limitCount) {
		String key = Stream.of(accessKey, resourcePin).filter(StringUtils::isNotBlank).collect(Collectors.joining(":"));
		Incremented incremented = countLimiter.incrementDate(key, System.currentTimeMillis(), 2, TimeUnit.DAYS,
				limitCount);
		if (BooleanUtils.isTrue(incremented.getAccessed())) {
			return incremented;
		} else {
			throw new AppExceedingDailyMaximumUseTimesLimitException("dailyMaximumUseTimes: " + limitCount);
		}
	}
}
