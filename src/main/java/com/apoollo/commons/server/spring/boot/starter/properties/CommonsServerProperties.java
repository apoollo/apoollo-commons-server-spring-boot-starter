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
public class CommonsServerProperties extends EnablePorperties {

	private PathProperties path;

	private String signatureSecret;

	private String keyPairAccessKeyProperty;

	private String keyPairSecretKeyProperty;
	
	private String jwtTokenProperty;

	private RabcProperties rbac;

	private AccessProperties access;

	private LoggerProperties log;
}
