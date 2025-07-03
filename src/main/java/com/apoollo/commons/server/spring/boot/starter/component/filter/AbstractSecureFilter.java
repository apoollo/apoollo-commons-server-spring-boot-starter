/**
 * 
 */
package com.apoollo.commons.server.spring.boot.starter.component.filter;

import java.io.IOException;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;

import com.apoollo.commons.server.spring.boot.starter.properties.PathProperties;
import com.apoollo.commons.util.request.context.RequestContext;
import com.apoollo.commons.util.request.context.model.RequestContextCapacitySupport;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * @author liuyulong
 * @since 2025-05-19
 */
public abstract class AbstractSecureFilter implements Filter {

	private static final PathMatcher PATH_MATCHER = new AntPathMatcher();
	private static final String ONCE_MATCHES_REQUEST_ATTRIBUTE = AbstractSecureFilter.class + "onceMatchesRequest";

	protected PathProperties pathProperties;
	protected RequestContextCapacitySupport requestContextSupport;

	public AbstractSecureFilter(PathProperties pathProperties, RequestContextCapacitySupport requestContextSupport) {
		super();
		this.pathProperties = pathProperties;
		this.requestContextSupport = requestContextSupport;
	}

	@Override
	public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain)
			throws IOException, ServletException {
		HttpServletRequest request = (HttpServletRequest) servletRequest;
		HttpServletResponse response = (HttpServletResponse) servletResponse;
		if (matches(request)) {
			try {
				chain.doFilter(doPreSecureFilter(request, response), response);
			} catch (Exception e) {
				if (BooleanUtils.isTrue(
						requestContextSupport.writeExceptionResponse(response, RequestContext.get(), e, () -> true))) {
					return;
				} else {
					throw e;
				}
			} finally {
				cleanupMatches(request, response, chain);
			}
		} else {
			try {
				chain.doFilter(request, response);
			} finally {
				cleanupNoMaches(request, response, chain);
			}
		}
	}

	public abstract HttpServletRequest doPreSecureFilter(HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException;

	public void cleanupMatches(HttpServletRequest request, HttpServletResponse response, FilterChain chain) {

	}

	public void cleanupNoMaches(HttpServletRequest request, HttpServletResponse response, FilterChain chain) {

	}

	protected void cleanAttribute(HttpServletRequest request) {
		request.removeAttribute(ONCE_MATCHES_REQUEST_ATTRIBUTE);
	}

	public boolean matches(HttpServletRequest request) {
		Boolean matches = (Boolean) request.getAttribute(ONCE_MATCHES_REQUEST_ATTRIBUTE);
		if (null == matches) {
			String requestMappingPath = RequestContext.getRequestMappingPath(request.getContextPath(),
					request.getRequestURI());
			request.setAttribute(ONCE_MATCHES_REQUEST_ATTRIBUTE, matches = matches(requestMappingPath));
		}
		return matches;
	}

	public boolean matches(String requestMappingPath) {
		boolean result = false;
		if (null != pathProperties) {
			if (matches(false, pathProperties.getExcludePathPatterns(), requestMappingPath)) {
				result = false;
			} else {
				result = matches(false, pathProperties.getIncludePathPatterns(), requestMappingPath);
			}
		}
		return result;
	}

	public boolean matches(boolean initail, List<String> pathPatterns, String requestMappingPath) {
		boolean result = initail;
		if (CollectionUtils.isNotEmpty(pathPatterns)) {
			result = pathPatterns.stream().filter(pattern -> PATH_MATCHER.match(pattern, requestMappingPath)).findAny()
					.isPresent();
		}
		return result;
	}

}
