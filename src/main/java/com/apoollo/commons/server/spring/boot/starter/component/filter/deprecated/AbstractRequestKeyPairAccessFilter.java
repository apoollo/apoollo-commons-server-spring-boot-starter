/**
 * 
 */
package com.apoollo.commons.server.spring.boot.starter.component.filter.deprecated;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.apoollo.commons.server.spring.boot.starter.compatible.CompatibleUtils;
import com.apoollo.commons.server.spring.boot.starter.component.filter.AbstractSecureFilter;
import com.apoollo.commons.server.spring.boot.starter.properties.PathProperties;
import com.apoollo.commons.server.spring.boot.starter.service.Access;
import com.apoollo.commons.util.LangUtils;
import com.apoollo.commons.util.request.context.RequestContext;
import com.apoollo.commons.util.request.context.RequestResource;
import com.apoollo.commons.util.request.context.core.AccessStrategy;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * @author liuyulong
 * @since 2025-05-22
 */
public abstract class AbstractRequestKeyPairAccessFilter extends AbstractSecureFilter {

	private static final Logger LOGGER = LoggerFactory.getLogger(AbstractRequestKeyPairAccessFilter.class);

	private Access<String> access;
	private String accessKeyProperty;
	private String secretKeyProperty;

	public AbstractRequestKeyPairAccessFilter(PathProperties pathProperties, Access<String> access,
			String accessKeyProperty, String secretKeyProperty) {
		super(pathProperties);
		this.access = access;
		this.accessKeyProperty = LangUtils.defaultString(accessKeyProperty, "accessKey");
		this.secretKeyProperty = LangUtils.defaultString(secretKeyProperty, "secretKey");
	}

	@Override
	public void doSecureFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws IOException, ServletException {

		RequestContext requestContext = RequestContext.getRequired();
		RequestResource requestResource = requestContext.getRequestResource();
		if (support(requestResource.getAccessStrategy())) {
			String accessKeyValue = getValue(request, accessKeyProperty);
			String secretKeyValue = getValue(request, secretKeyProperty);
			access.access(CompatibleUtils.compatibleStringSpace(accessKeyValue),
					CompatibleUtils.compatibleStringSpace(secretKeyValue));
			LOGGER.info("key pair validate accessed");
		}
		chain.doFilter(request, response);
	}

	public abstract boolean support(AccessStrategy accessStrategy);

	public abstract String getValue(HttpServletRequest request, String property);

}
