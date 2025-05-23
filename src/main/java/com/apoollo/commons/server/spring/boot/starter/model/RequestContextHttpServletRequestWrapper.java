/**
 * 
 */
package com.apoollo.commons.server.spring.boot.starter.model;

import java.io.IOException;

import com.apoollo.commons.server.spring.boot.starter.model.ServletInputStreamHelper.ByteArrayServletInputStream;
import com.apoollo.commons.util.request.context.RequestContext;

import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;

/**
 * @author liuyulong
 * @since 2025-05-23
 */
public class RequestContextHttpServletRequestWrapper extends CommonsHttpServletRequestWrapper {

	private RequestContext requestContext;

	public RequestContextHttpServletRequestWrapper(HttpServletRequest request, RequestContext requestContext) {
		super(request);
		this.requestContext = requestContext;
	}

	@Override
	public ServletInputStream getInputStream() throws IOException {
		byte[] requestBody = requestContext.getRequestBody();
		if (null == requestBody) {
			requestBody = ServletInputStreamHelper.getBodyByteArray(getRequest());
			requestContext.setRequestBody(requestBody);
		}
		return new ByteArrayServletInputStream(requestBody);
	}

	

}
