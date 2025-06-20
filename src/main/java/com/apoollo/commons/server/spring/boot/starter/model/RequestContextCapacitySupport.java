/**
 * 
 */
package com.apoollo.commons.server.spring.boot.starter.model;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;

import com.apoollo.commons.util.LangUtils;
import com.apoollo.commons.util.request.context.RequestContext;
import com.apoollo.commons.util.request.context.limiter.WrapResponseHandler;
import com.apoollo.commons.util.request.context.limiter.support.CapacitySupport;

import jakarta.servlet.http.HttpServletResponse;

/**
 * @author liuyulong
 * @since 2025-06-10
 */
public class RequestContextCapacitySupport {

	private WrapResponseHandler wrapResponseHandler;
	private CapacitySupport capacitySupport;

	public RequestContextCapacitySupport(WrapResponseHandler wrapResponseHandler, CapacitySupport capacitySupport) {
		super();
		this.wrapResponseHandler = wrapResponseHandler;
		this.capacitySupport = capacitySupport;
		if (BooleanUtils.isTrue(capacitySupport.getEnableCapacity())) {
			if (StringUtils.isBlank(capacitySupport.getResourcePin())) {
				throw new RuntimeException("platform capacitySupport resourcePin must not be blank");
			}
		}
	}

	public WrapResponseHandler getWrapResponseHandler(RequestContext requestContext) {
		WrapResponseHandler wrapResponseHandler = CapacitySupport.getAbility(requestContext, capacitySupport,
				CapacitySupport::getWrapResponseHandler);
		if (null == wrapResponseHandler) {
			wrapResponseHandler = this.wrapResponseHandler;
		}
		return wrapResponseHandler;
	}

	public Object getNormallyResponse(RequestContext requestContext, Object object) {
		Object result = null;
		if (null != requestContext && CapacitySupport.supportAbility(requestContext, capacitySupport,
				CapacitySupport::getEnableResponseWrapper)) {
			requestContext.setResponseTime(System.currentTimeMillis());
			result = getWrapResponseHandler(requestContext).getNormallyResponse(requestContext, object);
		}
		return result;
	}

	public <T> T writeExceptionResponse(HttpServletResponse response, RequestContext requestContext, Exception ex,
			Supplier<T> supplier) {
		T result = null;
		if (!response.isCommitted() && null != requestContext && CapacitySupport.supportAbility(requestContext,
				capacitySupport, CapacitySupport::getEnableResponseWrapper)) {
			requestContext.setResponseTime(System.currentTimeMillis());
			getWrapResponseHandler(requestContext).writeExceptionResponse(response, requestContext, ex);
			result = supplier.get();
		}
		return result;
	}

	public boolean supportAbility(RequestContext requestContext, Function<CapacitySupport, Boolean> function) {
		return CapacitySupport.supportAbility(requestContext, capacitySupport, function);
	}

	public void doSupport(Consumer<CapacitySupport> consumer) {
		CapacitySupport.doSupport(List.of(capacitySupport), consumer);
	}

	public void doSupport(RequestContext requestContext, Consumer<CapacitySupport> consumer) {
		CapacitySupport.doSupport(LangUtils
				.getStream(capacitySupport, requestContext.getUser(), requestContext.getRequestResource()).toList(),
				consumer);
	}

}
