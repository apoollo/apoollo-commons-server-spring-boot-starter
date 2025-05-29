/**
 * 
 */
package com.apoollo.commons.server.spring.boot.starter.service.impl;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.apoollo.commons.server.spring.boot.starter.service.Instance;

/**
 * @author liuyulong
 * @since 2025-05-29
 */
public class DefaultInstance implements Instance, ApplicationContextAware {

	private static final Map<String, Object> INSTANCE_CACHE = new ConcurrentHashMap<>();

	private ApplicationContext applicationContext;

	@Override
	public <T> T getInstance(Class<T> clazz) {
		try {
			T instance = applicationContext.getBean(clazz);
			if (null == instance) {
				instance = clazz.getDeclaredConstructor().newInstance();
			}
			return instance;
		} catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException
				| RuntimeException e) {
			throw new RuntimeException(e);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getInstance(String clazz) {
		Object object = INSTANCE_CACHE.get(clazz);
		if (null == object) {
			synchronized (INSTANCE_CACHE) {
				if (null == (object = INSTANCE_CACHE.get(clazz))) {
					try {
						object = getInstance(Class.forName(clazz));
					} catch (ClassNotFoundException e) {
						throw new RuntimeException(e);
					}
					INSTANCE_CACHE.put(clazz, object);
				}
			}
		}
		return (T) object;
	}

	public ApplicationContext getApplicationContext() {
		return applicationContext;
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

}
