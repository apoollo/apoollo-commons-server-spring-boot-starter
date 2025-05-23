/**
 * 
 */
package com.apoollo.commons.server.spring.boot.starter.model;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

/**
 * @author liuyulong
 * @since 2025-05-23
 */
public class CommonsHttpServletRequestWrapper extends HttpServletRequestWrapper {

	public CommonsHttpServletRequestWrapper(HttpServletRequest request) {
		super(request);
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
