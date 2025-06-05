/**
 * 
 */
package com.apoollo.commons.server.spring.boot.starter.limiter.core;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.apoollo.commons.server.spring.boot.starter.limiter.NonceLimiter;
import com.apoollo.commons.server.spring.boot.starter.limiter.support.NonceLimiterSupport;
import com.apoollo.commons.server.spring.boot.starter.model.Constants;
import com.apoollo.commons.util.exception.AppIllegalArgumentException;
import com.apoollo.commons.util.request.context.NonceValidator;

import jakarta.servlet.http.HttpServletRequest;

/**
 * @author liuyulong
 * @since 2025-06-05
 */
public class DefaultNonceLimiter implements NonceLimiter {

	private static final Logger LOGGER = LoggerFactory.getLogger(DefaultNonceLimiter.class);

	private NonceValidator nonceValidator;

	public DefaultNonceLimiter(NonceValidator nonceValidator) {
		super();
		this.nonceValidator = nonceValidator;
	}

	public void limit(HttpServletRequest request, NonceLimiterSupport nonceLimiterSupport) {

		String timestamp = request.getHeader(Constants.REQUEST_HEADER_TIMESTAMP);
		if (StringUtils.isBlank(timestamp)) {
			throw new AppIllegalArgumentException(
					"header [" + Constants.REQUEST_HEADER_TIMESTAMP + "] must not be null");
		}
		Long expireTimestampLong = nonceLimiterSupport.getNonceLimiterDuration();
		if (null == expireTimestampLong) {
			throw new RuntimeException("nonceDuration must not be null");
		}
		try {
			expireTimestampLong += Long.valueOf(timestamp);
		} catch (NumberFormatException e) {
			LOGGER.error("parse timestamp error:", e);
			throw new AppIllegalArgumentException("parse timestamp [" + timestamp + "] error");
		}
		long currentTimestamp = System.currentTimeMillis();
		if (currentTimestamp > expireTimestampLong) {
			LOGGER.error("currentTimestamp :" + currentTimestamp + ", expireTimestamp:" + expireTimestampLong);
			throw new AppIllegalArgumentException("timestamp [" + timestamp + "] already expire");
		}
		String nonce = request.getHeader(Constants.REQUEST_HEADER_NONCE);
		if (StringUtils.isBlank(nonce)) {
			throw new AppIllegalArgumentException("header [" + Constants.REQUEST_HEADER_NONCE + "] must not be null");
		}
		NonceValidator nonceValidator = nonceLimiterSupport.getNonceLimiterValidator();
		if (null == nonceValidator) {
			nonceValidator = this.nonceValidator;
		}
		if (!nonceValidator.isValid(nonce, nonceLimiterSupport.getNonceLimiterDuration())) {
			throw new AppIllegalArgumentException("header [" + Constants.REQUEST_HEADER_NONCE + "] invalid");
		}
	}
}
