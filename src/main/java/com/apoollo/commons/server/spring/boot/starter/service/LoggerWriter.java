/**
 * 
 */
package com.apoollo.commons.server.spring.boot.starter.service;

import com.apoollo.commons.util.model.Processor;
import com.apoollo.commons.util.request.context.RequestContext;

/**
 * @author liuyulong
 * @since 2023年8月22日
 */
public interface LoggerWriter {

	public void write(RequestContext requestContext, Processor after);
	
}
