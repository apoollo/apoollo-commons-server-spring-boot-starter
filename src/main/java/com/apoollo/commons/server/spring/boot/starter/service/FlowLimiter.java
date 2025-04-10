/**
 * 
 */
package com.apoollo.commons.server.spring.boot.starter.service;

/**
 * @author liuyulong
 */
public interface FlowLimiter {

	public void tryAccess(String accessKey, String resourcePin, Long limitCount);
}
