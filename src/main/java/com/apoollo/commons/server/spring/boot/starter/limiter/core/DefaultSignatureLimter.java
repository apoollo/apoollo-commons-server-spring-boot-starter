/**
 * 
 */
package com.apoollo.commons.server.spring.boot.starter.limiter.core;

import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.function.Supplier;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.apoollo.commons.server.spring.boot.starter.limiter.SignatureLimter;
import com.apoollo.commons.server.spring.boot.starter.limiter.support.SignatureLimterSupport;
import com.apoollo.commons.server.spring.boot.starter.model.Constants;
import com.apoollo.commons.server.spring.boot.starter.model.ServletInputStreamHelper;
import com.apoollo.commons.util.HttpContentUtils;
import com.apoollo.commons.util.crypto.hash.HmacSHA256;
import com.apoollo.commons.util.crypto.hash.MacHash;
import com.apoollo.commons.util.exception.AppIllegalArgumentException;

import jakarta.servlet.http.HttpServletRequest;

/**
 * @author liuyulong
 * @since 2025-06-05
 */
public class DefaultSignatureLimter implements SignatureLimter {

	private static final Logger LOGGER = LoggerFactory.getLogger(DefaultSignatureLimter.class);

	private static final MacHash HMAC_SHA256 = new HmacSHA256();

	@Override
	public void limit(HttpServletRequest request, SignatureLimterSupport signatureLimterSupport,
			Supplier<byte[]> body) {

		String requestSignature = request.getHeader(Constants.REQUEST_HEADER_SIGNATURE);
		if (StringUtils.isBlank(requestSignature)) {
			throw new AppIllegalArgumentException(
					"header [" + Constants.REQUEST_HEADER_SIGNATURE + "] must not be null");
		}
		String secret = signatureLimterSupport.getSignatureLimiterSecret();
		if (StringUtils.isBlank(secret)) {
			throw new RuntimeException("secret must not be blank");
		}
		String requestMehtod = request.getMethod();
		String requestUri = request.getRequestURI();
		String queryString = request.getQueryString();
		TreeMap<String, String> headers = getHeaders(request, signatureLimterSupport.getSignatureLimiterExcludeHeaderNames(),
				signatureLimterSupport.getSignatureLimiterIncludeHeaderNames());
		Charset charset = ServletInputStreamHelper.getCharset(request);
		String httpContent = HttpContentUtils.getHttpContent(charset, requestMehtod, requestUri, queryString, headers,
				body.get());
		String signature = HttpContentUtils.getHttpContentSignature(HMAC_SHA256, secret, charset, httpContent);

		if (!StringUtils.equals(requestSignature, signature)) {
			LOGGER.error("secret:" + secret);
			LOGGER.error("signature:" + signature);
			LOGGER.error("httpContent:\n" + httpContent);
			throw new AppIllegalArgumentException("signature compared false");
		}
	}

	public TreeMap<String, String> getNameValues(Function<String, String> valueGetter, Iterator<String> names,
			List<String> excludes, List<String> includes) {
		TreeMap<String, String> treeMap = new TreeMap<>();
		names.forEachRemaining(name -> {
			name = name.toLowerCase();
			if ((null == excludes || excludes.isEmpty() || !excludes.contains(name))
					&& (null == includes || includes.isEmpty() || includes.contains(name))) {
				String value = valueGetter.apply(name);
				treeMap.put(name, value);
			}
		});
		return treeMap;
	}

	public TreeMap<String, String> getHeaders(HttpServletRequest request, List<String> excludes,
			List<String> includes) {
		if (CollectionUtils.isEmpty(excludes)) {
			excludes = List.of(Constants.REQUEST_HEADER_SIGNATURE);
		}
		return getNameValues(headerName -> request.getHeader(headerName), request.getHeaderNames().asIterator(),
				excludes, includes);
	}

}
