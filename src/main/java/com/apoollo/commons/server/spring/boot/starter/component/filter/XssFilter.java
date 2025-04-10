/**
 * 
 */
package com.apoollo.commons.server.spring.boot.starter.component.filter;

import java.io.IOException;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;

import com.apoollo.commons.server.spring.boot.starter.service.XssHandler;
import com.apoollo.commons.server.spring.boot.starter.service.impl.DefaultXssHandler;

/**
 * liuyulong
 */
public class XssFilter implements Filter {

	private XssHandler xssHandler;

	/**
	 * @param xssHandler
	 */
	public XssFilter(XssHandler xssHandler) {
		super();
		this.xssHandler = xssHandler;
	}

	public XssFilter() {
		xssHandler = new DefaultXssHandler();
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		chain.doFilter(new XssHttpServletRequestWrapper((HttpServletRequest) request, xssHandler), response);
	}

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		// nothing
	}
}
