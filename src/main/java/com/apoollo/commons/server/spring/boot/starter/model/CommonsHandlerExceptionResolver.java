/**
 * 
 */
package com.apoollo.commons.server.spring.boot.starter.model;

import org.springframework.core.Ordered;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

import com.apoollo.commons.util.request.context.RequestContext;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * @author liuyulong
 * @since 2025-06-16
 */
public class CommonsHandlerExceptionResolver implements HandlerExceptionResolver, Ordered {

	private int order;
	private RequestContextSupport requestContextSupport;

	public CommonsHandlerExceptionResolver(int order, RequestContextSupport requestContextSupport) {
		super();
		this.order=order;
		this.requestContextSupport = requestContextSupport;
	}

	@Override
	public int getOrder() {
		return order;
	}

	@Override
	public ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response, Object handler,
			Exception ex) {
		return requestContextSupport.writeExceptionResponse(response, RequestContext.get(), ex, ModelAndView::new);
	}

}
