/**
 * 
 */
package com.apoollo.commons.server.spring.boot.starter.component.filter;

import java.io.IOException;

import org.apache.commons.lang3.BooleanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.apoollo.commons.server.spring.boot.starter.limiter.ContentEscapeHandler;
import com.apoollo.commons.server.spring.boot.starter.model.ContentEscapeHttpServletRequestWrapper;
import com.apoollo.commons.server.spring.boot.starter.properties.PathProperties;
import com.apoollo.commons.util.request.context.RequestContext;
import com.apoollo.commons.util.request.context.RequestResource;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * liuyulong
 */
public class RequestContentEscapeFilter extends AbstractSecureFilter {

	private static final Logger LOGGER = LoggerFactory.getLogger(RequestContentEscapeFilter.class);

	private ContentEscapeHandler contentEscapeHandler;

	public RequestContentEscapeFilter(PathProperties pathProperties, ContentEscapeHandler contentEscapeHandler) {
		super(pathProperties);
		this.contentEscapeHandler = contentEscapeHandler;
	}

	@Override
	public void doSecureFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		RequestContext requestContext = RequestContext.getRequired();
		RequestResource requestResource = requestContext.getRequestResource();
		if (BooleanUtils.isTrue(requestResource.getEnableContentEscape())) {
			request = new ContentEscapeHttpServletRequestWrapper((HttpServletRequest) request, contentEscapeHandler);
			LOGGER.info("content escape accessed");
		}
		chain.doFilter(request, response);
	}
}
