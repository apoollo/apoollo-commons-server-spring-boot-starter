/**
 * 
 */
package com.apoollo.commons.server.spring.boot.starter.properties;

import lombok.Getter;
import lombok.Setter;

/**
 * @author liuyulong
 */
@Getter
@Setter
public class RequestSecretKeyTokenAccessInterceptorProperties extends InterceptorCommonsProperties {

	private Boolean enable;

	private String accessKeyProperty;

	private String secretKeyProperty;
	
}
