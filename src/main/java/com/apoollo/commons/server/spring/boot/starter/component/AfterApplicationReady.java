/**
 * 
 */
package com.apoollo.commons.server.spring.boot.starter.component;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.core.Ordered;

/**
 * @author liuyulong
 * @since 2023年8月30日
 */
public interface AfterApplicationReady extends Ordered {

    public void onApplicationEvent(ApplicationReadyEvent event);
}
