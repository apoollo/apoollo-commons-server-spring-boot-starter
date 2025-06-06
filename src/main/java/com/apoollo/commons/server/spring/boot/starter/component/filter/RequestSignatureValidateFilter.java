/**
 * 
 */
package com.apoollo.commons.server.spring.boot.starter.component.filter;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;
import java.util.function.Function;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.apoollo.commons.server.spring.boot.starter.properties.PathProperties;
import com.apoollo.commons.util.HttpContentUtils;
import com.apoollo.commons.util.LangUtils;
import com.apoollo.commons.util.crypto.hash.HmacSHA256;
import com.apoollo.commons.util.crypto.hash.MacHash;
import com.apoollo.commons.util.exception.AppIllegalArgumentException;
import com.apoollo.commons.util.request.context.RequestContext;
import com.apoollo.commons.util.request.context.RequestResource;
import com.apoollo.commons.util.request.context.model.RequestConstants;
import com.apoollo.commons.util.request.context.model.ServletInputStreamHelper;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * @author liuyulong
 * @since 2025-05-19
 */
public class RequestSignatureValidateFilter extends AbstractSecureFilter {

	private static final Logger LOGGER = LoggerFactory.getLogger(RequestSignatureValidateFilter.class);

	private static final MacHash MAC_HASH = new HmacSHA256();
	private String secret;

	public RequestSignatureValidateFilter(PathProperties pathProperties, String secret) {
		super(pathProperties);
		this.secret = secret;
	}

	@Override
	public void doSecureFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws IOException, ServletException {

		RequestContext requestContext = RequestContext.getRequired();
		RequestResource requestResource = requestContext.getRequestResource();

		if (BooleanUtils.isTrue(requestResource.getEnableSignatureLimiter())) {
			String requestSignature = request.getHeader(RequestConstants.REQUEST_HEADER_SIGNATURE);
			if (StringUtils.isBlank(requestSignature)) {
				throw new AppIllegalArgumentException(
						"header [" + RequestConstants.REQUEST_HEADER_SIGNATURE + "] must not be null");
			}

			String targetSecret = LangUtils.defaultString(requestResource.getSignatureLimiterSecret(), this.secret);
			if (StringUtils.isBlank(targetSecret)) {
				throw new RuntimeException("secret must not be blank");
			}

			String requestMehtod = request.getMethod();
			String requestPath = getRequestPath(requestContext);
			String queryString = request.getQueryString();
			TreeMap<String, String> headers = getHeaders(request,
					requestResource.getSignatureLimiterExcludeHeaderNames(),
					requestResource.getSignatureLimiterIncludeHeaderNames());
			Charset charset = ServletInputStreamHelper.getCharset(request);
			byte[] body = ServletInputStreamHelper.getCachingBodyByteArray(requestContext, request);

			String httpContent = HttpContentUtils.getHttpContent(charset, requestMehtod, requestPath, queryString,
					headers, body);
			String signature = HttpContentUtils.getHttpContentSignature(MAC_HASH, targetSecret, charset, httpContent);

			if (!StringUtils.equals(requestSignature, signature)) {
				LOGGER.error("secret:" + targetSecret);
				LOGGER.error("signature:" + signature);
				LOGGER.error("httpContent:\n" + httpContent);
				throw new AppIllegalArgumentException("signature compared false");
			}
			LOGGER.info("body signature validate accessed");

		}
		chain.doFilter(request, response);
	}

	public String getRequestPath(RequestContext requestContext) {
		if (StringUtils.isBlank(requestContext.getContextPath())) {
			return requestContext.getRequestUri();
		} else {
			return StringUtils.join(requestContext.getContextPath(), "/", requestContext.getRequestUri());
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
			excludes = List.of(RequestConstants.REQUEST_HEADER_SIGNATURE);
		}
		return getNameValues(headerName -> request.getHeader(headerName), request.getHeaderNames().asIterator(),
				excludes, includes);
	}

}
