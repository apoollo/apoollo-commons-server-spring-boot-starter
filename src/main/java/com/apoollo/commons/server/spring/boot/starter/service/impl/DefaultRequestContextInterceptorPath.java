/**
 * 
 */
package com.apoollo.commons.server.spring.boot.starter.service.impl;

import java.util.List;

import com.apoollo.commons.server.spring.boot.starter.model.InterceptorConfigMerger;
import com.apoollo.commons.server.spring.boot.starter.properties.PathProperties;
import com.apoollo.commons.server.spring.boot.starter.service.RequestContextInterceptorPath;

/**
 * @author liuyulong
 */
public class DefaultRequestContextInterceptorPath implements RequestContextInterceptorPath {

	private InterceptorConfigMerger interceptorConfigMerger;

	/**
	 * @param commonsIntercetptor
	 * @param requestContextIntercetptor
	 */
	public DefaultRequestContextInterceptorPath(PathProperties commonsIntercetptor,
			PathProperties requestContextIntercetptor) {
		super();
		interceptorConfigMerger = new InterceptorConfigMerger(commonsIntercetptor, requestContextIntercetptor);
	}

	@Override
	public List<String> getPathPatterns() {
		return interceptorConfigMerger.getPathPatterns();
	}

	@Override
	public List<String> getExcludePathPatterns() {
		return interceptorConfigMerger.getExcludePathPatterns();
	}

}
