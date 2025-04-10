/**
 * 
 */
package com.apoollo.commons.server.spring.boot.starter.service.impl;

import java.util.List;

import com.apoollo.commons.server.spring.boot.starter.service.LoggerWriter;
import com.apoollo.commons.util.LangUtils;
import com.apoollo.commons.util.model.Processor;
import com.apoollo.commons.util.request.context.RequestContext;
import com.apoollo.commons.util.request.context.RequestContextDataBus;

/**
 * @author liuyulong
 * @since 2023年8月22日
 */
public class DefaultLoggerWriter implements LoggerWriter {

	private List<RequestContextDataBus> requestContextDataBuses;

	/**
	 * @param requestContextDataBuses
	 */
	public DefaultLoggerWriter(List<RequestContextDataBus> requestContextDataBuses) {
		super();
		this.requestContextDataBuses = requestContextDataBuses;
	}

	@Override
	public void write(RequestContext requestContext, Processor after) {
		LangUtils.getStream(requestContextDataBuses)
				.forEach(requestContextDataBus -> requestContextDataBus.transport(requestContext));
		if (null != after) {
			after.process();
		}
	}
}
