/**
 * 
 */
package com.apoollo.commons.server.spring.boot.starter.limiter.support;

import java.util.List;

/**
 * @author liuyulong
 * @since 2025-06-05
 */
public interface IpLimterSupport {
	
	Boolean getEnableIpLimiter();
	
	List<String> getIpLimiterExcludes();
	
	List<String> getIpLimiterIncludes();
}
