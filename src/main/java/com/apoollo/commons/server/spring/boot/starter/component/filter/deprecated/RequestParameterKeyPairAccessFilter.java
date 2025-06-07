/**
 * 
 */
package com.apoollo.commons.server.spring.boot.starter.component.filter.deprecated;

import com.apoollo.commons.server.spring.boot.starter.properties.PathProperties;
import com.apoollo.commons.server.spring.boot.starter.service.Access;
import com.apoollo.commons.util.request.context.core.AccessStrategy;

import jakarta.servlet.http.HttpServletRequest;

/**
 * @author liuyulong
 * @since 2025-05-22
 */
public class RequestParameterKeyPairAccessFilter extends AbstractRequestKeyPairAccessFilter {

	public RequestParameterKeyPairAccessFilter(PathProperties pathProperties, Access<String> access,
			String accessKeyProperty, String secretKeyProperty) {
		super(pathProperties, access, accessKeyProperty, secretKeyProperty);
	}

	@Override
	public boolean support(AccessStrategy accessStrategy) {
		return AccessStrategy.PRIVATE_PARAMETER_KEY_PAIR == accessStrategy;
	}

	@Override
	public String getValue(HttpServletRequest request, String property) {
		return request.getParameter(property);
	}

}
