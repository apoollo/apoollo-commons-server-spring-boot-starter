/**
 * 
 */
package com.apoollo.commons.server.spring.boot.starter.properties;

import lombok.Getter;
import lombok.Setter;

/**
 * @author liuyulong
 * @since 2023年8月22日
 */
@Getter
@Setter
public class CommonsServerProperties {

	private Boolean enable;
	
	private LoggerProperties log;
	
	private FilterProperties xssFilter;

	private InterceptorCommonsProperties commonsIntercetptor;

	private RequestContextInterceptorProperties requestContextInterceptor;

	private RequestResourceInterceptorProperties requestResourceInterceptor;

	private RequestHeaderJwtTokenAccessInterceptorProperties requestHeaderJwtTokenAccessInterceptor;

	private RequestSecretKeyTokenAccessInterceptorProperties requestSecretKeyTokenAccessInterceptor;

	private EnablePorperties requestBodyKeepParameterAdvice;
	
	private RequestBodyDigestValidateAdviceProperties requestBodyDigestValidateAdvice;

	private EnablePorperties requestBodyJwtTokenAccessAdvice;
	
	private EnablePorperties requestBodySecretKeyTokenAccessAdvice;

	private EnablePorperties responseBodyContext;

	private RabcProperties rbac;

	private AccessProperties access;
	
	private CacheProperties cache;

}
