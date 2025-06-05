/**
 * 
 */
package com.apoollo.commons.server.spring.boot.starter.limiter.core;

import java.time.Duration;

import org.apache.commons.lang3.BooleanUtils;

import com.apoollo.commons.server.spring.boot.starter.limiter.CorsLimiter;
import com.apoollo.commons.server.spring.boot.starter.limiter.DailyCountLimiter;
import com.apoollo.commons.server.spring.boot.starter.limiter.FlowLimiter;
import com.apoollo.commons.server.spring.boot.starter.limiter.IpLimter;
import com.apoollo.commons.server.spring.boot.starter.limiter.Limiters;
import com.apoollo.commons.server.spring.boot.starter.limiter.NonceLimiter;
import com.apoollo.commons.server.spring.boot.starter.limiter.RefererLimiter;
import com.apoollo.commons.server.spring.boot.starter.limiter.SignatureLimter;
import com.apoollo.commons.server.spring.boot.starter.limiter.SyncLimiter;
import com.apoollo.commons.server.spring.boot.starter.limiter.support.CorsLimiterSupport;
import com.apoollo.commons.server.spring.boot.starter.limiter.support.DailyCountLimiterSupport;
import com.apoollo.commons.server.spring.boot.starter.limiter.support.FlowLimiterSupport;
import com.apoollo.commons.server.spring.boot.starter.limiter.support.IpLimterSupport;
import com.apoollo.commons.server.spring.boot.starter.limiter.support.NonceLimiterSupport;
import com.apoollo.commons.server.spring.boot.starter.limiter.support.RefererLimiterSupport;
import com.apoollo.commons.server.spring.boot.starter.limiter.support.SignatureLimterSupport;
import com.apoollo.commons.server.spring.boot.starter.limiter.support.SyncLimiterSupport;
import com.apoollo.commons.server.spring.boot.starter.model.ServletInputStreamHelper;
import com.apoollo.commons.util.request.context.RequestContext;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * @author liuyulong
 * @since 2025-06-05
 */
public class DefaultLimters<T extends NonceLimiterSupport & SignatureLimterSupport & CorsLimiterSupport & IpLimterSupport & RefererLimiterSupport & SyncLimiterSupport & FlowLimiterSupport & DailyCountLimiterSupport>
		implements Limiters<T> {

	private NonceLimiter nonceLimiter;
	private SignatureLimter signatureLimter;
	private CorsLimiter corsLimiter;
	private IpLimter ipLimter;
	private RefererLimiter refererLimiter;
	private SyncLimiter syncLimiter;
	private FlowLimiter flowLimiter;
	private DailyCountLimiter dailyCountLimiter;

	public DefaultLimters(NonceLimiter nonceLimiter, SignatureLimter signatureLimter, CorsLimiter corsLimiter,
			IpLimter ipLimter, RefererLimiter refererLimiter, SyncLimiter syncLimiter, FlowLimiter flowLimiter,
			DailyCountLimiter dailyCountLimiter) {
		super();
		this.nonceLimiter = nonceLimiter;
		this.signatureLimter = signatureLimter;
		this.corsLimiter = corsLimiter;
		this.ipLimter = ipLimter;
		this.refererLimiter = refererLimiter;
		this.syncLimiter = syncLimiter;
		this.flowLimiter = flowLimiter;
		this.dailyCountLimiter = dailyCountLimiter;
	}

	@Override
	public void limit(HttpServletRequest request, HttpServletResponse response, RequestContext requestContext,
			T support) {
		if (BooleanUtils.isTrue(support.getEnableNonceLimiter())) {
			nonceLimiter.limit(request, support);
		}
		if (BooleanUtils.isTrue(support.getEnableSignatureLimiter())) {
			signatureLimter.limit(request, support,
					() -> ServletInputStreamHelper.getCachingBodyByteArray(requestContext, request));
		}
		if (BooleanUtils.isTrue(support.getEnableCorsLimiter())) {
			corsLimiter.limit(request, response, support);
		}
		if (BooleanUtils.isTrue(support.getEnableIpLimiter())) {
			ipLimter.limit(support, requestContext.getRequestIp());
		}
		if (BooleanUtils.isTrue(support.getEnableRefererLimiter())) {
			refererLimiter.limit(request, support);
		}
		if (BooleanUtils.isTrue(support.getEnableSyncLimiter())) {
			syncLimiter.limit(support.getAccessKey(), support.getResourcePin(), Duration.ofSeconds(30));
		}
		if (BooleanUtils.isTrue(support.getEnableFlowLimiter())) {
			flowLimiter.limit(support.getAccessKey(), support.getResourcePin(), support.getFlowLimiterLimitCount());
		}
		if (BooleanUtils.isTrue(support.getEnableDailyCountLimiter())) {
			dailyCountLimiter.limit(support.getAccessKey(), support.getResourcePin(),
					support.getDailyCountLimiterLimitCount());
		}
	}

	@Override
	public void unlimit(HttpServletRequest request, HttpServletResponse response, RequestContext requestContext,
			T support) {
		if (BooleanUtils.isTrue(support.getEnableSyncLimiter())) {
			syncLimiter.unlimit(support.getAccessKey(), support.getResourcePin());
		}
	}

}
