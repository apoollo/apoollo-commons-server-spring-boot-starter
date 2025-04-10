/**
 * 
 */
package com.apoollo.commons.server.spring.boot.starter.properties;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author liuyulong
 * @since 2023年8月30日
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RequestHeaderJwtTokenAccessInterceptorProperties extends InterceptorCommonsProperties{
	
	private Boolean enable;
	
}
