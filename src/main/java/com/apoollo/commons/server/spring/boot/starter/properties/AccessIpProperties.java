/**
 * 
 */
package com.apoollo.commons.server.spring.boot.starter.properties;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

/**
 * @author liuyulong
 */
@Getter
@Setter
public class AccessIpProperties {

	private Boolean enable;

	private List<String> whiteIpList;
	private List<String> blackIpList;
}
