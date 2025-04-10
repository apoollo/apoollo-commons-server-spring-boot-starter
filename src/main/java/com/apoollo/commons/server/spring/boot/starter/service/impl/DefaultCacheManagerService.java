/**
 * 
 */
package com.apoollo.commons.server.spring.boot.starter.service.impl;

import java.util.function.Consumer;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import com.apoollo.commons.server.spring.boot.starter.service.CacheManagerService;

/**
 * @author liuyulong
 */
public class DefaultCacheManagerService implements CacheManagerService {

    private CacheManager cacheManager;

    public DefaultCacheManagerService(CacheManager cacheManager) {
        super();
        this.cacheManager = cacheManager;
    }

    @Override
    public void doCache(String cacheName, Consumer<Cache> consumer) {
        if (null != cacheManager) {
            Cache cache = cacheManager.getCache(cacheName);
            if (null != cache) {
                consumer.accept(cache);
            }
        }
    }

}
