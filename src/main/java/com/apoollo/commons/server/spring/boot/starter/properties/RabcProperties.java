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
	
	private String keyPairAccessKeyProperty;

	private String keyPairSecretKeyProperty;
	
	private String jwtTokenProperty;

	private List<SerializableRequestResource> requestResources;
	
	private List<SerializableUser> users;

	// accessKey:requestSourcePins
	private Map<String, List<String>> accessKeyAndRequestResourcePinsMapping;

}
