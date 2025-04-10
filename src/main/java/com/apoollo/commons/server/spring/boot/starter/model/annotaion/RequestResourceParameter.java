/**
 * 
 */
package com.apoollo.commons.server.spring.boot.starter.model.annotaion;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * @author liuyulong
 * @since 2024-11-23
 */
@Getter
@Setter
@AllArgsConstructor
public class RequestResourceParameter {

	private int index;
	private Class<?> parameterType;
	private String parameterName;
	private Object parameterValue;
	private Logable logable;

}
