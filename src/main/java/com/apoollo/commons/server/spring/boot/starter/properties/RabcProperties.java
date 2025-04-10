/**
 * 
 */
package com.apoollo.commons.server.spring.boot.starter.properties;

import java.util.List;
import java.util.Map;

import com.apoollo.commons.util.request.context.def.DefaultRequestAccessParameter;
import com.apoollo.commons.util.request.context.def.DefaultRequestResource;
import com.apoollo.commons.util.request.context.def.DefaultUser;

import lombok.Getter;
import lombok.Setter;

/**
 * @author liuyulong
 */
@Getter
@Setter
public class RabcProperties {

	private List<DefaultUser> users;

	private List<DefaultRequestResource> requestResources;

	// accessKey:requestSourcePin:DefaultRequestAccessParameter
	private Map<String, Map<String, DefaultRequestAccessParameter>> permissions;

}
