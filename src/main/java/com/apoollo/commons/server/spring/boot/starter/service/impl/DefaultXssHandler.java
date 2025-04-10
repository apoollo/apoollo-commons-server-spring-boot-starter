/**
 * 
 */
package com.apoollo.commons.server.spring.boot.starter.service.impl;

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.MediaType;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.filter.ValueFilter;
import com.apoollo.commons.server.spring.boot.starter.service.XssHandler;

/**
 * liuyulong
 */
public class DefaultXssHandler implements XssHandler {

	private static final Map<String, String> DEFAULT_XSS_ESCAPE_MAPPING = new LinkedHashMap<>() {

		private static final long serialVersionUID = 7460765420084253552L;
		{
			put("<", "&lt;");
			put(">", "&gt;");
		}
	};

	public Map<String, String> getXssEscapeMapping() {

		return DEFAULT_XSS_ESCAPE_MAPPING;
	}

	@Override
	public boolean hasInsecure(String value) {
		return StringUtils.isNotBlank(value)
				&& getXssEscapeMapping().keySet().stream().filter(value::contains).findAny().isPresent();
	}

	@Override
	public String escape(String input) {
		if (StringUtils.isNotBlank(input)) {
			StringBuilder escaped = new StringBuilder(input.length() * 2);
			for (int i = 0; i < input.length(); i++) {
				Character character = input.charAt(i);
				String reference = getXssEscapeMapping().get(character.toString());
				if (null != reference) {
					escaped.append(reference);
				} else {
					escaped.append(character);
				}
			}
			input = escaped.toString();
		}
		return input;
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

	private class XssValueFilter implements ValueFilter {

		@Override
		public Object apply(Object object, String name, Object value) {
			if (value instanceof String) {
				return escape((String) value);
			}
			return value;
		}
	}

	@Override
	public String escapeBody(String contentType, String content) {
		if (StringUtils.startsWithIgnoreCase(contentType, MediaType.APPLICATION_JSON_VALUE)) {
			content = JSON.toJSONString(JSON.parse(content), new XssValueFilter());
		}
		return content;
	}

}
