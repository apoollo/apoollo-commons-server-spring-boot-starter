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

import com.apoollo.commons.server.spring.boot.starter.compatible.CompatibleUtils;
import com.apoollo.commons.server.spring.boot.starter.model.Constants;
import com.apoollo.commons.server.spring.boot.starter.service.Access;
import com.apoollo.commons.util.exception.AppException;
import com.apoollo.commons.util.request.context.RequestContext;
import com.apoollo.commons.util.request.context.RequestResource;
import com.apoollo.commons.util.request.context.UserKeyPair;
import com.apoollo.commons.util.request.context.def.AccessStrategy;

/**
 * @author liuyulong
 */
@ControllerAdvice
public class RequestBodyKeyPairAccessAdvice extends RequestBodyAdviceAdapter implements Ordered {

	private static final Logger LOGGER = LoggerFactory.getLogger(RequestBodyKeyPairAccessAdvice.class);

	private Access<String> access;

	public RequestBodyKeyPairAccessAdvice(Access<String> access) {
		super();
		this.access = access;
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
			if (AccessStrategy.PRIVATE_BODY_KEY_PAIR == requestResource.getAccessStrategy()) {
				if (body instanceof UserKeyPair) {
					UserKeyPair userKeyPair = (UserKeyPair) body;
					access.access(CompatibleUtils.compatibleStringSpace((userKeyPair.getAccessKey())),
							CompatibleUtils.compatibleStringSpace(userKeyPair.getSecretKey()));
					LOGGER.info("body key pair accessed");
				} else {
					throw new AppException("requestBody must implements [" + UserKeyPair.class + "]");
				}
			}
		}
		return super.afterBodyRead(body, inputMessage, parameter, targetType, converterType);
	}

	@Override
	public int getOrder() {
		return Constants.REQUEST_SECRET_KEY_TOKEN_ACCESS_BODY_ADVICE_ORDER;
	}

}
