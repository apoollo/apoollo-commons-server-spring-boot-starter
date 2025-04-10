/**
 * 
 */
package com.apoollo.commons.server.spring.boot.starter.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.apoollo.commons.util.request.context.RequestContext;
import com.apoollo.commons.util.request.context.RequestContextDataBus;

/**
 * @author liuyulong
 * @since 2023年8月22日
 */
public class DefaultRequestContextDataBus implements RequestContextDataBus {

	private static final Logger LOGGER = LoggerFactory.getLogger(DefaultRequestContextDataBus.class);

	@Override
	public void transport(RequestContext requestContext) {
		if (null != requestContext.getResponseTime()) {
			LOGGER.info("request elapsedTime：" + requestContext.getElapsedTime() + "ms");
		} else {
			LOGGER.info("request elapsedTime：" + (System.currentTimeMillis() - requestContext.getRequestTime()) + "ms");
		}
	}

}
