/**
 * 
 */
package com.apoollo.commons.server.spring.boot.starter.component.filter;

import java.io.IOException;

import com.apoollo.commons.server.spring.boot.starter.model.ContentEscapeHttpServletRequestWrapper;
import com.apoollo.commons.server.spring.boot.starter.model.RequestContextSupport;
import com.apoollo.commons.server.spring.boot.starter.properties.PathProperties;
import com.apoollo.commons.util.request.context.RequestContext;
import com.apoollo.commons.util.request.context.limiter.ContentEscapeHandler;
import com.apoollo.commons.util.request.context.limiter.support.CapacitySupport;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * liuyulong
 */
public class RequestContentEscapeFilter extends AbstractSecureFilter {

	private ContentEscapeHandler contentEscapeHandler;

	public RequestContentEscapeFilter(PathProperties pathProperties, ContentEscapeHandler contentEscapeHandler,
			RequestContextSupport requestContextSupport) {
		super(pathProperties, requestContextSupport);
		this.contentEscapeHandler = contentEscapeHandler;
	}

	@Override
	public void doPreSecureFilter(HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
		RequestContext requestContext = RequestContext.getRequired();
		if (requestContextSupport.supportAbility(requestContext, CapacitySupport::getEnableContentEscape)) {
			request = new ContentEscapeHttpServletRequestWrapper(request, requestContext, contentEscapeHandler);
		}
	}

}
