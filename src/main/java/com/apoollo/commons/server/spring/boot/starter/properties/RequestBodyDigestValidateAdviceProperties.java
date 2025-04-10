/**
 * 
 */
package com.apoollo.commons.server.spring.boot.starter.properties;

import lombok.Getter;
import lombok.Setter;

/**
 * @author liuyulong
 * @since 2023年9月22日
 */
@Getter
@Setter
public class RequestBodyDigestValidateAdviceProperties {

	private Boolean enable;
	private String secret;
	
}
