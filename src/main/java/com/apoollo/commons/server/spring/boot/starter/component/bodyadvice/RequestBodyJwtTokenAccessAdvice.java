/**
 * 
 */
package com.apoollo.commons.server.spring.boot.starter.component.bodyadvice;

import java.io.IOException;
import java.lang.reflect.Type;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.MethodParameter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdviceAdapter;

import com.apoollo.commons.server.spring.boot.starter.model.Constants;
import com.apoollo.commons.server.spring.boot.starter.service.Access;
import com.apoollo.commons.util.JwtUtils.JwtToken;
import com.apoollo.commons.util.exception.AppException;
import com.apoollo.commons.util.request.context.RequestContext;
import com.apoollo.commons.util.request.context.RequestResource;
import com.apoollo.commons.util.request.context.TokenGetter;
import com.apoollo.commons.util.request.context.access.AuthorizationJwtTokenDecoder;
import com.apoollo.commons.util.request.context.core.AccessStrategy;

/**
 * @author liuyulong
 */
@ControllerAdvice
public class RequestBodyJwtTokenAccessAdvice extends RequestBodyAdviceAdapter implements Ordered {

	private static final Logger LOGGER = LoggerFactory.getLogger(RequestBodyJwtTokenAccessAdvice.class);

	private AuthorizationJwtTokenDecoder authorizationJwtTokenJwtTokenDecoder;
	private Access<JwtToken> access;

	public RequestBodyJwtTokenAccessAdvice(AuthorizationJwtTokenDecoder authorizationJwtTokenJwtTokenDecoder,
			Access<JwtToken> access) {
		super();
		this.access = access;
		this.authorizationJwtTokenJwtTokenDecoder = authorizationJwtTokenJwtTokenDecoder;
	}

	@Override
	public boolean supports(MethodParameter methodParameter, Type targetType,
			Class<? extends HttpMessageConverter<?>> converterType) {
		return true;
	}

	@Override
	public HttpInputMessage beforeBodyRead(HttpInputMessage inputMessage, MethodParameter parameter, Type targetType,
			Class<? extends HttpMessageConverter<?>> converterType) throws IOException {
		return super.beforeBodyRead(inputMessage, parameter, targetType, converterType);
	}

	@Override
	public Object afterBodyRead(Object body, HttpInputMessage inputMessage, MethodParameter parameter, Type targetType,
			Class<? extends HttpMessageConverter<?>> converterType) {
		RequestContext requestContext = RequestContext.get();
		if (null != requestContext) {
			RequestResource requestResource = requestContext.getRequestResource();
			if (null != requestResource
					&& AccessStrategy.PRIVATE_BODY_JWT_TOKEN == requestResource.getAccessStrategy()) {
				if (body instanceof TokenGetter) {
					TokenGetter tokenGetter = (TokenGetter) body;
					JwtToken jwtToken = authorizationJwtTokenJwtTokenDecoder
							.decode(tokenGetter.getAuthorizationJwtToken());
					access.access(jwtToken.getAccessKey(), jwtToken);
					LOGGER.info("body jwt token accessed");
				} else {
					throw new AppException("requestBody must implements [" + TokenGetter.class + "]");
				}
			}
		}
		return super.afterBodyRead(body, inputMessage, parameter, targetType, converterType);
	}

	@Override
	public int getOrder() {
		return Constants.REQUEST_BODY_JWT_TOKEN_ACCESS_ADVICE_ORDER;
	}

}
