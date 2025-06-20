/**
 * 
 */
package com.apoollo.commons.server.spring.boot.starter.properties;

import java.util.List;
import java.util.Map;

import com.apoollo.commons.util.request.context.access.core.DefaultRequestResource.SerializableRequestResource;
import com.apoollo.commons.util.request.context.access.core.DefaultUser.SerializableUser;

import lombok.Getter;
import lombok.Setter;

/**
 * @author liuyulong
 */
@Getter
@Setter
public class RabcProperties {

	private List<SerializableUser> users;

	private List<SerializableRequestResource> requestResources;

	// accessKey:requestSourcePins
	private Map<String, List<String>> accessKeyAndRequestResourcePinsMapping;

}
