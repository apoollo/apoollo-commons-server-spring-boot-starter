/**
 * 
 */
package com.apoollo.commons.server.spring.boot.starter.service;

import java.time.Duration;

/**
 * @author liuyulong
 * @since 2024-11-29
 */
public interface SyncService {

	public boolean lock(String key, Duration duration);

	public void unlock(String key);
}
