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
public class LoggerProperties {

	private String fileLogPath;
	private String messageLogPath;
	private String messageLoggerName;
}
