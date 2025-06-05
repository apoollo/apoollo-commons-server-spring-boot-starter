/**
 * 
 */
package com.apoollo.commons.server.spring.boot.starter.limiter.support;

/**
 * @author liuyulong
 * @since 2025-06-05
 */
public interface PrincipalSupport {

	String getResourcePin();

	String getAccessKey();
}
