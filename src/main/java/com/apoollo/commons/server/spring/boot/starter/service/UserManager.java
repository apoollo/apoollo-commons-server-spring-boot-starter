/**
 * 
 */
package com.apoollo.commons.server.spring.boot.starter.service;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import com.apoollo.commons.util.JwtUtils;
import com.apoollo.commons.util.JwtUtils.Renewal;
import com.apoollo.commons.util.request.context.RequestContext;
import com.apoollo.commons.util.request.context.User;

/**
 * @author liuyulong
 * @since 2023年8月30日
 */
public interface UserManager {

	// public static final String CACHE_NAME = "User";

	public User getUser(String accessKey);

	public void setUser(User user, Long timeout, TimeUnit timeUnit);

	public default void renewal(String accessKey, Renewal renewal) {

	}
	public default String login(User user, Long timeout, TimeUnit timeUnit) {
		int keepSeconds = ((Long) timeUnit.toSeconds(timeout)).intValue();
		String jwtToken = JwtUtils.generateJwtToken(user.getAccessKey(), user.getSecretKey(),
				user.getSecretKeySaltValue(), new Date(), keepSeconds);
		setUser(user, timeout, timeUnit);
		return JwtUtils.authorizationConcatJwtToken(jwtToken);
	}

	public default void refresh(String accessKey, Boolean enable, String secretKey) {

	}

	public default void refresh(String accessKey, Boolean enable) {

	}

	public default void refresh(String accessKey, String secretKey) {

	}

	public default void delete(String accessKey) {

	}

	public default void logout() {
		delete(RequestContext.getRequired().getUser().getAccessKey());
	}

}
