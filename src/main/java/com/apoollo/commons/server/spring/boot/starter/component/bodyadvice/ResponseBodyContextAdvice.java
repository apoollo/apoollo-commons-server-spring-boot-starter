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
import com.apoollo.commons.util.request.context.Response;
import com.apoollo.commons.util.request.context.limiter.WrapResponseHandler;
import com.apoollo.commons.util.request.context.limiter.support.CapacitySupport;

/**
 * @author liuyulong
 * @since 2023年8月29日
 */
@ControllerAdvice
public class ResponseBodyContextAdvice extends WrapResponseSupport implements ResponseBodyAdvice<Object> {

	public ResponseBodyContextAdvice(CapacitySupport capacitySupport, WrapResponseHandler wrapResponseHandler) {
		super(capacitySupport, wrapResponseHandler);
	}

	@Override
	public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
		return true;
	}

	private Response<?> wrapResponseBody(RequestContext requestContext, Object body) {
		return handle(requestContext, wrapResponseHandler -> {
			Response<?> responseBody = null;
			if (body instanceof Response) {
				responseBody = (Response<?>) body;
				if (null == responseBody.getSuccess()) {
					responseBody.setSuccess(wrapResponseHandler.processIsExecuteSuccess(responseBody.getCode()));
				}
			} else {
				responseBody = wrapResponseHandler.success(body);
			}
			responseBody.setElapsedTime(requestContext.getElapsedTime());
			responseBody.setRequestId(requestContext.getRequestId());
			wrapResponseHandler.resetResponse(requestContext.getRequestBody(), responseBody);
			requestContext.setResponse(responseBody);
			return responseBody;
		});
	}

	@Override
	public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
			Class<? extends HttpMessageConverter<?>> selectedConverterType, ServerHttpRequest request,
			ServerHttpResponse response) {

		RequestContext requestContext = RequestContext.get();
		Response<?> responseBody = null;
		if (null != requestContext) {
			requestContext.setResponseTime(System.currentTimeMillis());
			responseBody = wrapResponseBody(requestContext, body);
		}
		Object responseBodyTarget = null;
		if (null == responseBody) {
			responseBodyTarget = body;
		} else {
			responseBodyTarget = responseBody;
		}
		return responseBodyTarget;
	}

}
