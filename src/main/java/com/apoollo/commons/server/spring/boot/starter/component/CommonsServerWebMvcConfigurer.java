/**
 * 
 */
package com.apoollo.commons.server.spring.boot.starter.component;

import java.util.Iterator;
import java.util.List;

import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author liuyulong
 */

public class CommonsServerWebMvcConfigurer implements WebMvcConfigurer {

	@Override
	public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {

		Iterator<HttpMessageConverter<?>> iterator = converters.iterator();
		while (iterator.hasNext()) {
			// 当前框架本意所有返回均为JSON，不支持单独的字符串返回
			if (iterator.next().getClass().isAssignableFrom(StringHttpMessageConverter.class)) {
				iterator.remove();
			}
		}
	}

}
