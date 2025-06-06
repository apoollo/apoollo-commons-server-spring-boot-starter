/**
 * 
 */
package com.apoollo.commons.server.spring.boot.starter.component.filter;

import java.io.IOException;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.apoollo.commons.server.spring.boot.starter.properties.PathProperties;
import com.apoollo.commons.util.exception.AppIllegalArgumentException;
import com.apoollo.commons.util.request.context.NonceValidator;
import com.apoollo.commons.util.request.context.RequestContext;
import com.apoollo.commons.util.request.context.RequestResource;
import com.apoollo.commons.util.request.context.model.RequestConstants;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * @author liuyulong
 * @since 2025-05-30
 */
public class RequestNonceValidateFilter extends AbstractSecureFilter {

	private static final Logger LOGGER = LoggerFactory.getLogger(RequestNonceValidateFilter.class);

	private NonceValidator nonceValidator;

	public RequestNonceValidateFilter(PathProperties pathProperties, NonceValidator nonceValidator) {
		super(pathProperties);
		this.nonceValidator = nonceValidator;
	}

	@Override
	public void doSecureFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws IOException, ServletException {

		RequestContext requestContext = RequestContext.getRequired();
		RequestResource requestResource = requestContext.getRequestResource();

		if (BooleanUtils.isTrue(requestResource.getEnableNonceLimiter())) {

			String timestamp = request.getHeader(RequestConstants.REQUEST_HEADER_TIMESTAMP);
			if (StringUtils.isBlank(timestamp)) {
				throw new AppIllegalArgumentException(
						"header [" + RequestConstants.REQUEST_HEADER_TIMESTAMP + "] must not be null");
			}
			long timestampLong = requestResource.getNonceLimiterDuration();
			try {
				timestampLong += Long.valueOf(timestamp);
			} catch (NumberFormatException e) {
				LOGGER.error("parse timestamp error:", e);
				throw new AppIllegalArgumentException("parse timestamp [" + timestamp + "] error");
			}
			long currentTimestamp = System.currentTimeMillis();
			if (currentTimestamp > timestampLong) {
				LOGGER.error("currentTimestamp :" + currentTimestamp + ", Timestamp:" + timestampLong);
				throw new AppIllegalArgumentException("timestamp [" + timestamp + "] already expire");
			}

			String nonce = request.getHeader(RequestConstants.REQUEST_HEADER_NONCE);
			if (StringUtils.isBlank(nonce)) {
				throw new AppIllegalArgumentException("header [" + RequestConstants.REQUEST_HEADER_NONCE + "] must not be null");
			}

			NonceValidator nonceValidator = requestResource.getNonceLimiterValidator();
			if (null == nonceValidator) {
				nonceValidator = this.nonceValidator;
			}
			if (!nonceValidator.isValid(nonce, requestResource.getNonceLimiterDuration())) {
				throw new AppIllegalArgumentException("header [" + RequestConstants.REQUEST_HEADER_NONCE + "] invalid");
			}
		}
		chain.doFilter(request, response);
	}

}
