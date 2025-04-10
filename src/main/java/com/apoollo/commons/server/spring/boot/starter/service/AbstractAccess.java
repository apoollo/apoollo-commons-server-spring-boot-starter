/**
 * 
 */
package com.apoollo.commons.server.spring.boot.starter.service;

import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.apoollo.commons.server.spring.boot.starter.properties.AccessIpProperties;
import com.apoollo.commons.server.spring.boot.starter.properties.AccessProperties;
import com.apoollo.commons.util.LangUtils;
import com.apoollo.commons.util.exception.AppExceedingDailyMaximumUseTimesLimitException;
import com.apoollo.commons.util.exception.AppForbbidenException;
import com.apoollo.commons.util.exception.detailed.AccessKeyEmptyException;
import com.apoollo.commons.util.exception.detailed.IpLimterException;
import com.apoollo.commons.util.exception.detailed.TokenEmptyExcetion;
import com.apoollo.commons.util.redis.service.CountLimiter;
import com.apoollo.commons.util.redis.service.impl.CommonsCountLimiter.Incremented;
import com.apoollo.commons.util.request.context.Authorization;
import com.apoollo.commons.util.request.context.RequestAccessParameter;
import com.apoollo.commons.util.request.context.RequestContext;
import com.apoollo.commons.util.request.context.RequestResource;
import com.apoollo.commons.util.request.context.User;
import com.apoollo.commons.util.request.context.model.Authorized;

/**
 * @author liuyulong
 */
public abstract class AbstractAccess<T> implements Access<T> {

	private static final Logger LOGGER = LoggerFactory.getLogger(AbstractAccess.class);

	protected UserManager userManager;
	protected Authorization<?> authorization;
	private AccessProperties accessProperties;
	private CommonsServerRedisKey commonsServerRedisKey;
	private CountLimiter countLimiter;
	private FlowLimiter flowLimiter;

	public AbstractAccess(UserManager userManager, Authorization<?> authorization,
			CommonsServerRedisKey commonsServerRedisKey, CountLimiter countLimiter, FlowLimiter flowLimiter,
			AccessProperties accessProperties) {
		super();
		this.userManager = userManager;
		this.authorization = authorization;
		this.commonsServerRedisKey = commonsServerRedisKey;
		this.countLimiter = countLimiter;
		this.flowLimiter = flowLimiter;
		this.accessProperties = accessProperties;
	}

	@Override
	public void access(String accessKey, T token) {
		if (StringUtils.isBlank(accessKey)) {
			throw new AccessKeyEmptyException("[accessKey] must not be blank");
		}
		if (null == token) {
			throw new TokenEmptyExcetion("[token] must not be null");
		}
		User user = userManager.getUser(accessKey);
		if (null == user) {
			throw new AppForbbidenException("Not Logged In : " + accessKey);
		}
		if (!BooleanUtils.isTrue(user.getEnable())) {
			throw new AppForbbidenException("user disabled : " + accessKey);
		}
		String secretKey = user.getSecretKey();
		if (StringUtils.isBlank(secretKey)) {
			throw new RuntimeException("can't find [secretKey] by accesskey : " + accessKey);
		}

		RequestContext requestContext = RequestContext.getRequired();
		// check ip
		limitIpAccess(user.getIpWhiteList(), requestContext.getRequestIp());

		// check acessKey and secretKey
		limitTokenAccess(user, token);
		requestContext.setUser(user);

		// check user resource permission
		authorized(requestContext);

		// limit flow enter
		limitFlowAccess(requestContext, accessKey);

		// check daily limit invoke times
		limitDailyRequestTimesAccess(requestContext, accessKey);

		LOGGER.info("accesskey：[" + user.getAccessKey() + "] accessed");
	}

	private Authorized<?> authorized(RequestContext requestContext) {
		Authorized<?> authorized = authorization.getAuthorized(requestContext.getUser(),
				requestContext.getRequestResource());
		if (null == authorized || !authorized.getSuccess()) {
			throw new AppForbbidenException("unauthorized");
		}
		requestContext.setAuthorized(authorized);
		return authorized;
	}

	private void limitFlowAccess(RequestContext requestContext, String accessKey) {
		Boolean flowLimit = LangUtils.getPropertyIfNotNull(accessProperties,
				(accessProperties) -> accessProperties.getLimitFlow());
		if (BooleanUtils.isTrue(flowLimit)) {
			RequestResource requestResource = requestContext.getRequestResource();
			RequestAccessParameter requestAccessParameter = requestContext
					.getAuthorizedValue(RequestAccessParameter.class);
			if (null != requestAccessParameter && null != requestAccessParameter.getRequestTimesPerSecond()) {
				flowLimiter.tryAccess(accessKey, requestResource.getResourcePin(),
						requestAccessParameter.getRequestTimesPerSecond());
			} else {
				flowLimiter.tryAccess(accessKey, requestResource.getResourcePin(), requestResource.getLimtUserQps());
			}
		}
	}

	private void limitDailyRequestTimesAccess(RequestContext requestContext, String accessKey) {
		Boolean accessDailyLimitInvocationTimes = LangUtils.getPropertyIfNotNull(accessProperties,
				(accessProperties) -> accessProperties.getLimitDailyRequestTimes());
		if (BooleanUtils.isTrue(accessDailyLimitInvocationTimes)) {
			RequestAccessParameter requestAccessParameter = requestContext
					.getAuthorizedValue(RequestAccessParameter.class);
			if (null != requestAccessParameter && null != requestAccessParameter.getRequestMaximumTimesToday()) {

				String key = commonsServerRedisKey.getCommonsServerKey(accessKey,
						requestContext.getRequestResource().getResourcePin());
				Incremented incremented = countLimiter.incrementDate(key, 2,
						requestAccessParameter.getRequestMaximumTimesToday());

				if (BooleanUtils.isTrue(incremented.getAccessed())) {
					requestContext.setDailyMaximumUseTimesLimitKey(incremented.getKey());
				} else {
					throw new AppExceedingDailyMaximumUseTimesLimitException(
							"dailyMaximumUseTimes: " + requestAccessParameter.getRequestMaximumTimesToday());
				}
			}
		}
	}

	private void limitIpAccess(List<String> accessIpList, String requestIp) {
		AccessIpProperties limitIp = LangUtils.getPropertyIfNotNull(accessProperties,
				(accessProperties) -> accessProperties.getLimitIp());
		if (null != requestIp && null != limitIp && BooleanUtils.isTrue(limitIp.getEnable())) {

			if (maches(limitIp.getBlackIpList(), requestIp)) {
				throw new IpLimterException("request ip disabled by local black ip list : " + requestIp);
			}
			if (!maches(limitIp.getWhiteIpList(), requestIp)) {
				if (CollectionUtils.isNotEmpty(accessIpList)) {
					if (!maches(accessIpList, requestIp)) {
						throw new IpLimterException("request ip disabled by white ip list : " + requestIp);
					}
				}
			}
		}
	}

	private boolean maches(List<String> list, String input) {
		return LangUtils.getStream(list).filter(whiteIp -> {
			boolean accessed = true;
			if (whiteIp.endsWith("*")) {
				String startWith = whiteIp.substring(0, whiteIp.length() - 1);
				accessed = input.startsWith(startWith);

			} else {
				accessed = whiteIp.equals(input);
			}
			return accessed;

		}).findFirst().isPresent();
	}

	public abstract void limitTokenAccess(User user, T token);
}
