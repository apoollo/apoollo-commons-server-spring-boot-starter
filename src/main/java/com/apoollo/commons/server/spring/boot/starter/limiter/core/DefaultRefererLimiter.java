/**
 * 
 */
package com.apoollo.commons.server.spring.boot.starter.limiter.core;

import org.apache.commons.collections4.CollectionUtils;

import com.apoollo.commons.server.spring.boot.starter.limiter.RefererLimiter;
import com.apoollo.commons.server.spring.boot.starter.limiter.support.RefererLimiterSupport;
import com.apoollo.commons.server.spring.boot.starter.model.Constants;
import com.apoollo.commons.util.UrlUtils;
import com.apoollo.commons.util.UrlUtils.Url;
import com.apoollo.commons.util.exception.AppIllegalArgumentException;

import jakarta.servlet.http.HttpServletRequest;

/**
 * @author liuyulong
 * @since 2025-06-05
 */
public class DefaultRefererLimiter implements RefererLimiter {

	@Override
	public void limit(HttpServletRequest request, RefererLimiterSupport refererLimiterSupport) {
		if (CollectionUtils.isNotEmpty(refererLimiterSupport.getRefererLimiterIncludeReferers())) {
			String referer = request.getHeader(Constants.REQUEST_HEADER_REFERER);
			Url url = UrlUtils.parse(referer);
			if (null != url) {
				String domain = url.getDomain();
				refererLimiterSupport.getRefererLimiterIncludeReferers().stream().forEach(includeReferer -> {
					boolean matches = false;
					if (includeReferer.startsWith("*")) {
						matches = domain.endsWith(includeReferer.substring(1));
					} else {
						matches = domain.equals(includeReferer);
					}
					if (!matches) {
						throw new AppIllegalArgumentException(
								"header [" + Constants.REQUEST_HEADER_REFERER + "]  not allowed");
					}
				});
			}
		}
	}

}
