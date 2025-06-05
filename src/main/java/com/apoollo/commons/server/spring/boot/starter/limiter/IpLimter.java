/**
 * 
 */
package com.apoollo.commons.server.spring.boot.starter.limiter;

import com.apoollo.commons.server.spring.boot.starter.limiter.support.IpLimterSupport;

/**
 * @author liuyulong
 * @since 2025-06-05
 */
public interface IpLimter {

	public void limit(IpLimterSupport ipLimterSupport, String requestIp);
}
