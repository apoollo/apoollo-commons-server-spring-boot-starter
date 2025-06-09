/**
 * 
 */
package com.apoollo.commons.server.spring.boot.starter.model;

import java.io.IOException;
import java.util.function.Function;

import com.apoollo.commons.util.request.context.RequestContext;
import com.apoollo.commons.util.request.context.model.ServletInputStreamHelper;

import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;

/**
 * @author liuyulong
 * @since 2025-05-23
 */
public class RequestContextHttpServletRequestWrapper extends CommonsHttpServletRequestWrapper {

	public RequestContextHttpServletRequestWrapper(HttpServletRequest request, RequestContext requestContext) {
		super(request, requestContext);
	}

	@Override
	public ServletInputStream getInputStream() throws IOException {
		return ServletInputStreamHelper.getCachingServletInputStream(requestContext, getRequest(), Function.identity());
	}

}
