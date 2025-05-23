/**
 * 
 */
package com.apoollo.commons.server.spring.boot.starter.properties;

import lombok.Getter;
import lombok.Setter;

/**
 * liuyulong
 */
@Getter
@Setter
public class FilterProperties extends PathProperties {

	private Boolean enable;
	private Integer order;
}
