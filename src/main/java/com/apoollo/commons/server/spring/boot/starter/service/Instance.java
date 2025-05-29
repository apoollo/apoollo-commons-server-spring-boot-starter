/**
 * 
 */
package com.apoollo.commons.server.spring.boot.starter.service;

import org.springframework.context.ApplicationContext;

/**
 * @author liuyulong
 * @since 2025-05-29
 */
public interface Instance {

	public ApplicationContext getApplicationContext();

	public <T> T getInstance(Class<T> clazz);

	public <T> T getInstance(String clazz);

}
