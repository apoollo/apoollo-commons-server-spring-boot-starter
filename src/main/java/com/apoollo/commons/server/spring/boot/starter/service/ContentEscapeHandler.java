/**
 * 
 */
package com.apoollo.commons.server.spring.boot.starter.service;

import java.nio.charset.Charset;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

/**
 * liuyulong
 */
public interface ContentEscapeHandler {

	public String escape(String value);

	public String[] escapes(String[] values);

	public Map<String, String[]> escapes(Map<String, String[]> map);

	public Enumeration<String> escapes(Enumeration<String> enumeration);

	public byte[] escapeByContentType(Charset charset, String contentType, byte[] content);

	public List<String> getSupportEscapeContentTypes();
}
