/**
 * 
 */
package com.apoollo.commons.server.spring.boot.starter.service;

import java.util.function.Consumer;

import org.springframework.cache.Cache;

/**
 * @author liuyulong
 */
public interface CacheManagerService {

    public void doCache(String cacheName, Consumer<Cache> consumer) ;
}
