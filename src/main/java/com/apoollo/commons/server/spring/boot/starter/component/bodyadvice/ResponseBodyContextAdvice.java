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

import com.apoollo.commons.util.LangUtils;
import com.apoollo.commons.util.request.context.HttpCodeNameHandler;
import com.apoollo.commons.util.request.context.RequestContext;
import com.apoollo.commons.util.request.context.Response;

/**
 * @author liuyulong
 * @since 2023年8月29日
 */
@ControllerAdvice
public class ResponseBodyContextAdvice implements ResponseBodyAdvice<Object> {

	public ResponseBodyContextAdvice() {
		super();
	}

	@Override
	public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
		return true;
	}

	@Override
	public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
			Class<? extends HttpMessageConverter<?>> selectedConverterType, ServerHttpRequest request,
			ServerHttpResponse response) {

		RequestContext requestContext = RequestContext.get();
		Response<?> responseBody = null;
		if (null != requestContext) {

			HttpCodeNameHandler codeNameHandler = requestContext.getDefaultResourceAccessStrategy()
					.getHttpCodeNameHandler();
			if (body instanceof Response) {
				responseBody = (Response<?>) body;
				if (null == responseBody.getSuccess()) {
					responseBody.setSuccess(codeNameHandler.processIsExecuteSuccess(responseBody.getCode()));
				}
			} else {
				responseBody = codeNameHandler.success(body);
			}
			requestContext.setResponseTime(System.currentTimeMillis());
			responseBody.setElapsedTime(requestContext.getElapsedTime());
			responseBody.setRequestId(requestContext.getRequestId());
			codeNameHandler.resetResponse(requestContext.getRequestBody(), responseBody);
			requestContext.beforeBodyWrite(responseBody);
		} else {
			if (body instanceof Response) {
				responseBody = (Response<?>) body;
				responseBody.setElapsedTime(0L);
				responseBody.setRequestId(LangUtils.getUppercaseUUID());
			}
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
