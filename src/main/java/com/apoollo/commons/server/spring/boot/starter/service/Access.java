/**
 * 
 */
package com.apoollo.commons.server.spring.boot.starter.service;

/**
 * @author liuyulong
 */
public interface Access<T> {

	public void access(String accessKey, T token);

}
