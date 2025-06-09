/**
 * 
 */
package com.apoollo.commons.server.spring.boot.starter.model;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

import com.apoollo.commons.util.request.context.RequestContext;
import com.apoollo.commons.util.request.context.model.ServletInputStreamHelper;

import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

/**
 * @author liuyulong
 * @since 2025-05-23
 */
public class CommonsHttpServletRequestWrapper extends HttpServletRequestWrapper {

	protected RequestContext requestContext;

	public CommonsHttpServletRequestWrapper(HttpServletRequest request, RequestContext requestContext) {
		super(request);
		this.requestContext = requestContext;
	}

	public Charset getCharset() {
		return ServletInputStreamHelper.getCharset(getRequest());
	}

	@Override
	public String getCharacterEncoding() {
		return ServletInputStreamHelper.getCharacterEncodingName(getRequest());
	}

	@Override
	public BufferedReader getReader() throws IOException {
		BufferedReader reader = null;
		ServletInputStream servletInputStream = this.getInputStream();
		if (null != servletInputStream) {
			reader = new BufferedReader(new InputStreamReader(servletInputStream, getCharacterEncoding()));
		}
		return reader;
	}
}
