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
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AccessProperties {

	private AccessIpProperties limitIp;
	private Boolean limitDailyRequestTimes;
	private Boolean limitFlow;

}
