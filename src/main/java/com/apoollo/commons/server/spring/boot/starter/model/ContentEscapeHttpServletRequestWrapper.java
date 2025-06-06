/**
 * 
 */
package com.apoollo.commons.server.spring.boot.starter.model;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import com.apoollo.commons.util.LangUtils;
import com.apoollo.commons.util.request.context.RequestContext;
import com.apoollo.commons.util.request.context.limiter.ContentEscapeHandler;
import com.apoollo.commons.util.request.context.model.ServletInputStreamHelper;

import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

/**
 * liuyulong
 */
public class ContentEscapeHttpServletRequestWrapper extends CommonsHttpServletRequestWrapper {

	private ContentEscapeHandler contentEscapeHandler;

	/**
	 * @param request
	 * @param contentEscapeHandler
	 */
	public ContentEscapeHttpServletRequestWrapper(HttpServletRequest request,
			ContentEscapeHandler contentEscapeHandler) {
		super(request);
		this.contentEscapeHandler = contentEscapeHandler;
	}

	@Override
	public String getHeader(String name) {
		return contentEscapeHandler.escape(super.getHeader(name));
	}

	@Override
	public Enumeration<String> getHeaders(String name) {
		return contentEscapeHandler.escapes(super.getHeaders(name));
	}

	@Override
	public String getParameter(String name) {
		return contentEscapeHandler.escape(super.getParameter(name));
	}

	@Override
	public String[] getParameterValues(String name) {
		return contentEscapeHandler.escapes(super.getParameterValues(name));
	}

	@Override
	public Map<String, String[]> getParameterMap() {
		return contentEscapeHandler.escapes(super.getParameterMap());
	}

	@Override
	public Cookie[] getCookies() {
		Cookie[] cookies = super.getCookies();
		if (ArrayUtils.isNotEmpty(cookies)) {
			for (int i = 0; i < cookies.length; i++) {
				Cookie cookie = cookies[i];
				cookie.setValue(contentEscapeHandler.escape(cookie.getValue()));
			}
		}
		return cookies;
	}

	@Override
	public ServletInputStream getInputStream() throws IOException {
		String contentType = super.getContentType();
		if (LangUtils//
				.getStream(contentEscapeHandler.getSupportEscapeContentTypes())//
				.filter(supportContentType -> {
					return StringUtils.startsWithIgnoreCase(contentType, supportContentType);
				})//
				.findAny()//
				.isPresent()//
		) {
			return ServletInputStreamHelper.getCachingServletInputStream(RequestContext.getRequired(),
					super.getRequest(), content -> {

						return contentEscapeHandler.escapeByContentType(getCharset(), contentType, content);
					});
		}
		return super.getInputStream();
	}

}
