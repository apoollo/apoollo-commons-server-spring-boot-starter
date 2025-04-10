/**
 * 
 */
package com.apoollo.commons.server.spring.boot.starter.service;

import java.util.Map;

/**
 * liuyulong
 */
public interface XssHandler {

	public boolean hasInsecure(String value);

	public String escape(String value);

	public String[] escapes(String[] values);
	
	public Map<String, String[]> escapes(Map<String, String[]> map);

	public String escapeBody(String contentType, String content);

}
