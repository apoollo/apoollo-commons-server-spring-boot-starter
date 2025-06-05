/**
 * 
 */
package com.apoollo.commons.server.spring.boot.starter.limiter;

import java.util.function.Supplier;

import com.apoollo.commons.server.spring.boot.starter.limiter.support.SignatureLimterSupport;

import jakarta.servlet.http.HttpServletRequest;

/**
 * @author liuyulong
 * @since 2025-06-05
 */
public interface SignatureLimter {

	public void limit(HttpServletRequest request, SignatureLimterSupport signatureLimterSupport, Supplier<byte[]> body);

}
