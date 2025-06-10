/**
 * 
 */
package com.apoollo.commons.server.spring.boot.starter.component.bodyadvice;

import java.util.function.Function;

import com.apoollo.commons.util.request.context.RequestContext;
import com.apoollo.commons.util.request.context.limiter.WrapResponseHandler;
import com.apoollo.commons.util.request.context.limiter.support.CapacitySupport;

/**
 * @author liuyulong
 * @since 2025-06-10
 */
public class WrapResponseSupport {

	private CapacitySupport capacitySupport;
	private WrapResponseHandler wrapResponseHandler;

	public WrapResponseSupport(CapacitySupport capacitySupport, WrapResponseHandler wrapResponseHandler) {
		super();
		this.capacitySupport = capacitySupport;
		this.wrapResponseHandler = wrapResponseHandler;
	}

	public <T> T handle(RequestContext requestContext, Function<WrapResponseHandler, T> function) {
		T result = null;
		if (CapacitySupport.supportAbility(requestContext, capacitySupport,
				CapacitySupport::getEnableResponseWrapper)) {
			WrapResponseHandler wrapResponseHandler = CapacitySupport.getAbility(requestContext, capacitySupport,
					CapacitySupport::getWrapResponseHandler);
			if (null == wrapResponseHandler) {
				wrapResponseHandler = this.wrapResponseHandler;
			}
			result = function.apply(wrapResponseHandler);
		}
		return result;
	}

}
