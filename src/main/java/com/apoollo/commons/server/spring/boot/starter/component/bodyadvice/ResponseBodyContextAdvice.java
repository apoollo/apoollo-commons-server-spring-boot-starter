/**
 * 
 */
package com.apoollo.commons.server.spring.boot.starter.component.bodyadvice;

import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import com.apoollo.commons.server.spring.boot.starter.model.RequestContextSupport;
import com.apoollo.commons.util.request.context.RequestContext;

/**
 * @author liuyulong
 * @since 2023年8月29日
 */
@ControllerAdvice
public class ResponseBodyContextAdvice implements ResponseBodyAdvice<Object> {

	private RequestContextSupport requestContextSupport;

	public ResponseBodyContextAdvice(RequestContextSupport requestContextSupport) {
		super();
		this.requestContextSupport = requestContextSupport;
	}

	@Override
	public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
		return true;
	}

	@Override
	public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
			Class<? extends HttpMessageConverter<?>> selectedConverterType, ServerHttpRequest request,
			ServerHttpResponse response) {
		Object responseBody = requestContextSupport.getNormallyResponse(RequestContext.get(), body);
		if (null == responseBody) {
			responseBody = body;
		}
		return responseBody;
	}

}
