/**
 * 
 */
package com.apoollo.commons.server.spring.boot.starter.limiter;

import com.apoollo.commons.server.spring.boot.starter.limiter.support.IpLimiterSupport;

/**
 * @author liuyulong
 * @since 2025-06-05
 */
public interface IpLimiter {

	public void limit(IpLimiterSupport ipLimterSupport, String requestIp);
}
