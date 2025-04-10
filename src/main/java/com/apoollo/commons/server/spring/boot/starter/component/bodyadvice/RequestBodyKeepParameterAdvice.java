/**
 * 
 */
package com.apoollo.commons.server.spring.boot.starter.component.bodyadvice;

import java.io.IOException;
import java.lang.reflect.Type;

import org.springframework.core.MethodParameter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdviceAdapter;

import com.apoollo.commons.server.spring.boot.starter.model.Constants;
import com.apoollo.commons.util.request.context.RequestContext;

/**
 * @author liuyulong
 */
@ControllerAdvice
public class RequestBodyKeepParameterAdvice extends RequestBodyAdviceAdapter implements Ordered {


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
		RequestContext.getRequired().setRequestBody(body);
		return super.afterBodyRead(body, inputMessage, parameter, targetType, converterType);
	}

	@Override
	public int getOrder() {
		return Constants.REQUEST_KEEP_PARAMETER_BODY_ADVICE_ORDER;
	}

}
