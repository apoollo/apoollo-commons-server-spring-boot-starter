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

import com.apoollo.commons.util.request.context.RequestContext;
import com.apoollo.commons.util.request.context.model.RequestContextCapacitySupport;

/**
 * @author liuyulong
 * @since 2023年8月29日
 */
@ControllerAdvice
public class ResponseBodyContextAdvice implements ResponseBodyAdvice<Object> {

	private RequestContextCapacitySupport requestContextSupport;

	public ResponseBodyContextAdvice(RequestContextCapacitySupport requestContextSupport) {
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
		Object wrappedBody = requestContextSupport.getNormallyResponse(RequestContext.get(), body);
		return null == wrappedBody ? body : wrappedBody;
	}

}
