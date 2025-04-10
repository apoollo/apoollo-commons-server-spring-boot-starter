/**
 * 
 */
package com.apoollo.commons.server.spring.boot.starter.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.apoollo.commons.server.spring.boot.starter.properties.AccessProperties;
import com.apoollo.commons.server.spring.boot.starter.service.AbstractAccess;
import com.apoollo.commons.server.spring.boot.starter.service.CommonsServerRedisKey;
import com.apoollo.commons.server.spring.boot.starter.service.FlowLimiter;
import com.apoollo.commons.server.spring.boot.starter.service.UserManager;
import com.apoollo.commons.util.JwtUtils;
import com.apoollo.commons.util.JwtUtils.JwtToken;
import com.apoollo.commons.util.exception.AppForbbidenException;
import com.apoollo.commons.util.redis.service.CountLimiter;
import com.apoollo.commons.util.request.context.Authorization;
import com.apoollo.commons.util.request.context.User;
import com.auth0.jwt.exceptions.SignatureVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;

/**
 * @author liuyulong
 */
public class JwtTokenAccess extends AbstractAccess<JwtToken> {

	private static final Logger LOGGER = LoggerFactory.getLogger(JwtTokenAccess.class);

	public JwtTokenAccess(UserManager userManager, Authorization<?> authorization,
			CommonsServerRedisKey commonsServerRedisKey, CountLimiter countLimiter, FlowLimiter flowLimiter,
			AccessProperties accessProperties) {
		super(userManager, authorization, commonsServerRedisKey, countLimiter, flowLimiter, accessProperties);
	}

	@Override
	public void limitTokenAccess(User user, JwtToken jwtToken) {
		try {
			JwtUtils.jwtVerify(jwtToken.getJwtTokenDecoded(), user.getSecretKey(), user.getSecretKeySaltValue());
		} catch (TokenExpiredException e) {
			LOGGER.error("signature expired:", e);
			throw new AppForbbidenException("signature expired");
		} catch (SignatureVerificationException e) {
			LOGGER.error("signature verify failed:", e);
			throw new AppForbbidenException("signature verify failed");
		} catch (Exception e) {
			LOGGER.error("signature verify error:", e);
			throw new AppForbbidenException("signature verify error");
		}
	}

}
