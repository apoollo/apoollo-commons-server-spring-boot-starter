/**
 * 
 */
package com.apoollo.commons.server.spring.boot.starter.model;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.AbstractHandlerExceptionResolver;

import com.apoollo.commons.util.exception.refactor.AppAuthenticationAccessKeyIllegalException;
import com.apoollo.commons.util.exception.refactor.AppAuthenticationJwtTokenExpiredException;
import com.apoollo.commons.util.exception.refactor.AppAuthenticationJwtTokenIllegalException;
import com.apoollo.commons.util.exception.refactor.AppAuthenticationKeyPairSecretKeyForbiddenException;
import com.apoollo.commons.util.exception.refactor.AppAuthenticationKeyPairTokenIllegalException;
import com.apoollo.commons.util.exception.refactor.AppAuthenticationTokenIllegalException;
import com.apoollo.commons.util.exception.refactor.AppAuthenticationUserDisabledException;
import com.apoollo.commons.util.exception.refactor.AppAuthorizationForbiddenException;
import com.apoollo.commons.util.exception.refactor.AppCorsLimiterRefusedException;
import com.apoollo.commons.util.exception.refactor.AppCountLimiterRefusedException;
import com.apoollo.commons.util.exception.refactor.AppException;
import com.apoollo.commons.util.exception.refactor.AppFlowLimiterRefusedException;
import com.apoollo.commons.util.exception.refactor.AppHttpCodeNameMessageException;
import com.apoollo.commons.util.exception.refactor.AppIpLimiterExcludeListRefusedException;
import com.apoollo.commons.util.exception.refactor.AppIpLimiterIncludeListRefusedException;
import com.apoollo.commons.util.exception.refactor.AppNonceLimiterNonceIllegalException;
import com.apoollo.commons.util.exception.refactor.AppNonceLimiterTimestampIllegalException;
import com.apoollo.commons.util.exception.refactor.AppRefererLimiterRefusedException;
import com.apoollo.commons.util.exception.refactor.AppRequestResourceDisabledException;
import com.apoollo.commons.util.exception.refactor.AppRequestResourceNotExistsException;
import com.apoollo.commons.util.exception.refactor.AppSignatureLimiterSignatureIllegalException;
import com.apoollo.commons.util.exception.refactor.AppSyncLimiterRefusedException;
import com.apoollo.commons.util.request.context.HttpCodeName;
import com.apoollo.commons.util.request.context.HttpCodeNameMessage;
import com.apoollo.commons.util.request.context.core.DefaultHttpCodeName;
import com.apoollo.commons.util.request.context.core.DefaultHttpCodeNameMessage;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * @author liuyulong
 * @since 2025-06-16
 */
public class CommonsHandlerExceptionResolver extends AbstractHandlerExceptionResolver {

	private static final Logger LOGGER = LoggerFactory.getLogger(CommonsHandlerExceptionResolver.class);

	private static final HttpCodeNameMessage<Integer, String, String> SYSTEM_ERROR = new DefaultHttpCodeNameMessage<>(
			5000, "SystemError", 200, "system error");

	private static final Map<Class<? extends AppException>, HttpCodeName<Integer, String>> CODE_NAME_EXCEPTION_MAPPING = new HashMap<>() {
		private static final long serialVersionUID = 8730699429353651670L;

		{
			// request resource
			put(AppRequestResourceNotExistsException.class,
					new DefaultHttpCodeName<>(4500, "RequestResourceNotExists", 200));
			put(AppRequestResourceDisabledException.class, new DefaultHttpCodeName<>(4501, "ResourceDisabled", 200));

			// nonce limiter
			put(AppNonceLimiterNonceIllegalException.class,
					new DefaultHttpCodeName<>(4510, "NonceLimiterNonceIllegal", 200));
			put(AppNonceLimiterTimestampIllegalException.class,
					new DefaultHttpCodeName<>(4511, "NonceLimiterTimestampIllegal", 200));

			// signature limiter
			put(AppSignatureLimiterSignatureIllegalException.class,
					new DefaultHttpCodeName<>(4520, "LimiterSignatureIllegal", 200));

			// cors limiter
			put(AppCorsLimiterRefusedException.class, new DefaultHttpCodeName<>(4530, "CorsLimiterRefused", 200));

			// ip limiter
			put(AppIpLimiterExcludeListRefusedException.class,
					new DefaultHttpCodeName<>(4540, "IpLimiterExcludeListRefused", 200));
			put(AppIpLimiterIncludeListRefusedException.class,
					new DefaultHttpCodeName<>(4541, "IpLimiterIncludeListRefused", 200));

			// referer limiter
			put(AppRefererLimiterRefusedException.class, new DefaultHttpCodeName<>(4550, "RefererLimiterRefused", 200));

			// sync limiter
			put(AppSyncLimiterRefusedException.class, new DefaultHttpCodeName<>(4560, "SyncLimiterRefused", 200));

			// flow limiter
			put(AppFlowLimiterRefusedException.class, new DefaultHttpCodeName<>(4570, "FlowLimiterRefused", 200));

			// counter limiter
			put(AppCountLimiterRefusedException.class, new DefaultHttpCodeName<>(4580, "CountLimiterRefused", 200));

			// authentication
			put(AppAuthenticationAccessKeyIllegalException.class,
					new DefaultHttpCodeName<>(4590, "AuthenticationAccessKeyIllegal", 200));
			put(AppAuthenticationTokenIllegalException.class,
					new DefaultHttpCodeName<>(4591, "AuthenticationTokenIllegal", 200));
			put(AppAuthenticationUserDisabledException.class,
					new DefaultHttpCodeName<>(4592, "AuthenticationUserDisabled", 200));

			// jwt token
			put(AppAuthenticationJwtTokenIllegalException.class,
					new DefaultHttpCodeName<>(4600, "AuthenticationJwtTokenIllegal", 200));
			put(AppAuthenticationJwtTokenExpiredException.class,
					new DefaultHttpCodeName<>(4601, "AuthenticationJwtTokenExpired", 200));

			// key pair
			put(AppAuthenticationKeyPairTokenIllegalException.class,
					new DefaultHttpCodeName<>(4610, "AuthenticationKeyPairTokenIllegal", 200));
			put(AppAuthenticationKeyPairSecretKeyForbiddenException.class,
					new DefaultHttpCodeName<>(4611, "AuthenticationKeyPairSecretKeyForbidden", 200));

			// authorization
			put(AppAuthorizationForbiddenException.class,
					new DefaultHttpCodeName<>(4620, "AuthorizationForbidden", 200));

		}
	};


	public CommonsHandlerExceptionResolver(int order) {
		super();
		setOrder(order);
	}

	@Override
	protected ModelAndView doResolveException(HttpServletRequest request, HttpServletResponse response, Object handler,
			Exception ex) {

		if (!response.isCommitted()) {

			HttpCodeNameMessage<Integer, String, String> httpCodeNameMessage = null;
			if (ex instanceof AppHttpCodeNameMessageException appException) {

				LOGGER.error(appException.getMessage(), ex);

				httpCodeNameMessage = new DefaultHttpCodeNameMessage<Integer, String, String>(appException.getCode(),
						appException.getName(), appException.getHttpCode(), appException.getMessage());

			} else if (ex instanceof AppException) {
				HttpCodeName<Integer, String> httpCodeName = CODE_NAME_EXCEPTION_MAPPING.get(ex.getClass());
				if (null != httpCodeName) {
					httpCodeNameMessage = new DefaultHttpCodeNameMessage<Integer, String, String>(
							httpCodeName.getCode(), httpCodeName.getName(), httpCodeName.getHttpCode(),
							ex.getMessage());
					LOGGER.error(ex.getMessage(), ex);
				}
			}
			if (null == httpCodeNameMessage) {
				LOGGER.error(SYSTEM_ERROR.getMessage(), ex);
				httpCodeNameMessage = SYSTEM_ERROR;
			}

			// TODO
			/*
			 * try { response.getOutputStream().write(null); } catch (IOException e) {
			 * e.printStackTrace(); }
			 */
		}

		return null;
	}

}
