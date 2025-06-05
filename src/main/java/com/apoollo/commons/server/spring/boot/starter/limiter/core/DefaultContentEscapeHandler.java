/**
 * 
 */
package com.apoollo.commons.server.spring.boot.starter.limiter.core;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import com.apoollo.commons.server.spring.boot.starter.limiter.ContentEscapeHandler;
import com.apoollo.commons.util.request.context.EscapeMethod;
import com.apoollo.commons.util.request.context.RequestContext;
import com.apoollo.commons.util.request.context.RequestResource;

/**
 * liuyulong
 */
public class DefaultContentEscapeHandler implements ContentEscapeHandler {

	private EscapeMethod escapeMethod;

	public DefaultContentEscapeHandler(EscapeMethod escapeMethod) {
		super();
		this.escapeMethod = escapeMethod;
	}

	public EscapeMethod getRequestContextEscapeMethod() {
		EscapeMethod escapeMethod = null;
		RequestContext requestContext = RequestContext.get();
		if (null != requestContext) {
			RequestResource requestResource = requestContext.getRequestResource();
			if (null != requestResource) {
				escapeMethod = requestResource.getContentEscapeMethod();
			}
		}
		if (null == escapeMethod) {
			escapeMethod = this.escapeMethod;
		}
		return escapeMethod;
	}

	@Override
	public String escape(String input) {
		return StringUtils.isNotBlank(input) ? getRequestContextEscapeMethod().escape(input) : input;
	}

	@Override
	public String[] escapes(String[] values) {
		if (ArrayUtils.isNotEmpty(values)) {
			for (int i = 0; i < values.length; i++) {
				values[i] = escape(values[i]);
			}
		}
		return values;
	}

	@Override
	public Map<String, String[]> escapes(Map<String, String[]> map) {
		Map<String, String[]> copyMap = new LinkedHashMap<>();
		if (MapUtils.isNotEmpty(map)) {
			map.forEach((key, valueArray) -> {
				if (ArrayUtils.isNotEmpty(valueArray)) {
					copyMap.put(key, escapes(valueArray));
				}
			});
		}
		return copyMap;
	}

	@Override
	public Enumeration<String> escapes(Enumeration<String> enumeration) {
		if (null != enumeration) {
			List<String> escapedHeaders = new ArrayList<>();
			while (enumeration.hasMoreElements()) {
				String value = enumeration.nextElement();
				escapedHeaders.add(escape(value));
			}
			enumeration = Collections.enumeration(escapedHeaders);
		}
		return enumeration;
	}

	@Override
	public byte[] escapeByContentType(Charset charset, String contentType, byte[] content) {
		return getRequestContextEscapeMethod().escapeByContentType(charset, contentType, content);
	}

	@Override
	public List<String> getSupportEscapeContentTypes() {
		return getRequestContextEscapeMethod().getSupportEscapeContentTypes();
	}

}
