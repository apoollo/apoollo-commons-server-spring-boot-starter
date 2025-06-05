/**
 * 
 */
package com.apoollo.commons.server.spring.boot.starter.limiter.core;

import java.io.IOException;

import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.cors.CorsProcessor;
import org.springframework.web.cors.DefaultCorsProcessor;

import com.apoollo.commons.server.spring.boot.starter.limiter.CorsLimiter;
import com.apoollo.commons.server.spring.boot.starter.limiter.support.CorsLimiterSupport;
import com.apoollo.commons.util.exception.AppIllegalArgumentException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * @author liuyulong
 * @since 2025-06-05
 */
public class DefaultCorsLimiter implements CorsLimiter {

	private CorsProcessor corsProcessor = new DefaultCorsProcessor() {
		@Override
		protected void rejectRequest(ServerHttpResponse response) throws IOException {
			throw new AppIllegalArgumentException("cors refused");
		}
	};

	@Override
	public void limit(HttpServletRequest request, HttpServletResponse response, CorsLimiterSupport corsLimiterSupport) {
		try {
			if (!corsProcessor.processRequest(corsLimiterSupport.getCorsLimiterConfiguration(), request, response)) {
				throw new AppIllegalArgumentException("cors refused");
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

	}

}
