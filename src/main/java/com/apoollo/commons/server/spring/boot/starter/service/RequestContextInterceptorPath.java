/**
 * 
 */
package com.apoollo.commons.server.spring.boot.starter.service;

import java.util.List;

/**
 * @author liuyulong
 */
public interface RequestContextInterceptorPath {

	public List<String> getPathPatterns();

	public List<String> getExcludePathPatterns();
}
