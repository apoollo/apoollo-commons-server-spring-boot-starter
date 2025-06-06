/**
 * 
 */
package com.apoollo.commons.server.spring.boot.starter.service.impl;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.apoollo.commons.server.spring.boot.starter.properties.AccessProperties;
import com.apoollo.commons.server.spring.boot.starter.service.AbstractAccess;
import com.apoollo.commons.server.spring.boot.starter.service.UserManager;
import com.apoollo.commons.util.exception.AppForbbidenException;
import com.apoollo.commons.util.exception.detailed.TokenEmptyExcetion;
import com.apoollo.commons.util.redis.service.CountLimiter;
import com.apoollo.commons.util.request.context.Authorization;
import com.apoollo.commons.util.request.context.User;
import com.apoollo.commons.util.request.context.limiter.FlowLimiter;

/**
 * @author liuyulong
 */
public class SecretKeyTokenAccess extends AbstractAccess<String> {

	private static final Logger LOGGER = LoggerFactory.getLogger(SecretKeyTokenAccess.class);

	public SecretKeyTokenAccess(UserManager userManager, Authorization<?> authorization, CountLimiter countLimiter,
			FlowLimiter flowLimiter, AccessProperties accessProperties) {
		super(userManager, authorization, countLimiter, flowLimiter, accessProperties);
	}

	@Override
	public void limitTokenAccess(User user, String token) {
		if (StringUtils.isBlank(token)) {
			throw new TokenEmptyExcetion("[token] must not be blank");
		}
		if (!StringUtils.equals(user.getSecretKey(), token)) {
			throw new AppForbbidenException("secretKey verify failed");
		}
		LOGGER.info("secret key equals access");
	}

}
