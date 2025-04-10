/**
 * 
 */
package com.apoollo.commons.server.spring.boot.starter.component;

import java.util.Iterator;
import java.util.List;

import org.springframework.context.ApplicationContext;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.apoollo.commons.server.spring.boot.starter.model.InterceptorConfigMerger;
import com.apoollo.commons.server.spring.boot.starter.model.annotaion.RequestResourceRegister;
import com.apoollo.commons.server.spring.boot.starter.properties.CommonsServerProperties;
import com.apoollo.commons.server.spring.boot.starter.service.InternalHandlerInterceptor;
import com.apoollo.commons.util.LangUtils;

/**
 * @author liuyulong
 */

public class CommonsServerWebMvcConfigurer implements WebMvcConfigurer {

	private static final String DEFAULT_PATH_PATTERN = "/**";

	private ApplicationContext applicationContext;
	private CommonsServerProperties commonsServerProperties;
	private List<InternalHandlerInterceptor> internalHandlerInterceptors;
	
	
	public CommonsServerWebMvcConfigurer(ApplicationContext applicationContext,
			CommonsServerProperties commonsServerProperties,
			List<InternalHandlerInterceptor> internalHandlerInterceptors) {
		super();
		this.applicationContext = applicationContext;
		this.commonsServerProperties = commonsServerProperties;
		this.internalHandlerInterceptors = internalHandlerInterceptors;
	}

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		RequestResourceRegister requestResourceRegister = new RequestResourceRegister(applicationContext,
				commonsServerProperties);
		requestResourceRegister.regist();

		LangUtils.getStream(internalHandlerInterceptors).forEach(internalHandlerInterceptor -> {
			InterceptorConfigMerger interceptorConfig = new InterceptorConfigMerger(
					commonsServerProperties.getCommonsIntercetptor(),
					internalHandlerInterceptor.getInterceptorCommonsProperties());

			List<String> pathPatterns = interceptorConfig.getPathPatterns();
			List<String> excludePathPatterns = interceptorConfig.getExcludePathPatterns();

			if (pathPatterns.isEmpty()) {
				pathPatterns = LangUtils.toList(DEFAULT_PATH_PATTERN);
			}
			registry.addInterceptor(internalHandlerInterceptor)//
					.order(internalHandlerInterceptor.getOrder())//
					.addPathPatterns(pathPatterns)//
					.excludePathPatterns(excludePathPatterns);

		});
	}

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
