/**
 * 
 */
package com.apoollo.commons.server.spring.boot.starter.model.annotaion;

import java.lang.reflect.Method;
import java.util.List;
import java.util.stream.IntStream;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.apoollo.commons.util.LangUtils;
import com.apoollo.commons.util.request.context.RequestContext;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * @author liuyulong
 * @since 2024-11-23
 */
@Aspect
public class RequestResourceAspect {

	private static final Logger LOGGER = LoggerFactory.getLogger(RequestResourceAspect.class);

	@Around(value = "@annotation(requestResource)")
	public Object advice(ProceedingJoinPoint point, RequestResource requestResource) throws Throwable {

		long startTime = System.currentTimeMillis();
		RequestContext requestContext = RequestContext.getRequired();

		MethodSignature signature = (MethodSignature) point.getSignature();

		Class<?>[] parameterTypes = signature.getParameterTypes();
		String[] parameterNames = signature.getParameterNames();
		Object[] parameterValues = point.getArgs();

		String requestLogName = StringUtils.join("[", requestContext.getRequestResource().getName(), "入参]: ");
		if (ArrayUtils.isNotEmpty(parameterTypes)) {
			List<RequestResourceParameter> requestResourceParameters = IntStream.range(0, parameterTypes.length)
					.mapToObj(i -> {
						Class<?> parameterType = parameterTypes[i];
						String parameterName = parameterNames[i];
						Object parameterValue = parameterValues[i];
						Logable logable = parameterType.getAnnotation(Logable.class);
						return new RequestResourceParameter(i, parameterType, parameterName, parameterValue, logable);
					})//
					.filter(parameter -> parameter.getParameterType() != HttpServletRequest.class
							&& parameter.getParameterType() != HttpServletResponse.class)
					.toList();

			if (requestResourceParameters.size() > 0) {

				// 入参打印日志
				requestResourceParameters.stream()
						.filter(parameter -> null == parameter.getLogable() || parameter.getLogable().enable())
						.forEach(paramter -> {
							String[] maskProperies = null == paramter.getLogable() ? null
									: paramter.getLogable().maskProperies();
							String requestLogContent = StringUtils.join(requestLogName, paramter.getParameterName(),
									" = ", LangUtils.toLoggingJsonString(paramter.getParameterValue(), maskProperies));
							LOGGER.info(requestLogContent);
						});
			} else {
				LOGGER.info(StringUtils.join(requestLogName, "无"));
			}
		} else {
			LOGGER.info(StringUtils.join(requestLogName, "无"));
		}

		Object object = point.proceed();

		// 出参打印日志
		String responseLogName = StringUtils.join("[", requestContext.getRequestResource().getName(), "出参]: ");
		Method method = signature.getMethod();
		if (method.getReturnType() != void.class && method.getReturnType() != Void.class) {
			Logable logable = method.getAnnotation(Logable.class);
			if (null == logable || null != logable && logable.enable()) {
				String[] maskProperies = null == logable ? null : logable.maskProperies();
				String logContent = StringUtils.join(responseLogName,
						LangUtils.toLoggingJsonString(object, maskProperies));
				LOGGER.info(logContent);
			}
		} else {
			LOGGER.info(StringUtils.join(responseLogName, "无"));
		}

		LOGGER.info(StringUtils.join("[", requestContext.getRequestResource().getName(), "耗时]: ",
				System.currentTimeMillis() - startTime, "(ms)"));

		return object;
	}
}
