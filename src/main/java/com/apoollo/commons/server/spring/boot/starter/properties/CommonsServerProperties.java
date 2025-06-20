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

	private PathProperties path;

	private RabcProperties rbac;

	private LoggerProperties log;
}
