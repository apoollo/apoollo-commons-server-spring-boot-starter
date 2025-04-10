/**
 * 
 */
package com.apoollo.commons.server.spring.boot.starter.service.impl;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.apoollo.commons.util.MdcUtils;
import com.apoollo.commons.util.model.Processor;
import com.apoollo.commons.util.request.context.RequestContext;
import com.apoollo.commons.util.request.context.RequestContextDataBus;

/**
 * @author liuyulong
 * @since 2023年8月22日
 */
public class AsyncLoggerWriter extends DefaultLoggerWriter {

	private static final Logger LOGGER = LoggerFactory.getLogger(AsyncLoggerWriter.class);

	/**
	 * @param requestContextDataBuses
	 */
	public AsyncLoggerWriter(List<RequestContextDataBus> requestContextDataBuses) {
		super(requestContextDataBuses);
	}

	@Override
	public void write(RequestContext requestContext, Processor after) {
		CompletableFuture.runAsync(() -> {
			MdcUtils.addAll(requestContext.getRequestId());
			super.write(requestContext, after);
		}).whenComplete((response, throwable) -> {
			MdcUtils.releaseAll();
			if (null != throwable) {
				LOGGER.error("AsyncLoggerWriter write error:", throwable);
			}
		});
	}
}
