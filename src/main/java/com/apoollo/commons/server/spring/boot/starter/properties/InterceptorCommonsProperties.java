/**
 * 
 */
package com.apoollo.commons.server.spring.boot.starter.properties;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

/**
 * @author liuyulong
 * @since 2023年8月31日
 */
@Getter
@Setter
public class InterceptorCommonsProperties {
	private List<String> pathPatterns;
	private List<String> excludePathPatterns;
}
