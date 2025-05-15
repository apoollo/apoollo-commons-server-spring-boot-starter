/**
 * 
 */
package com.apoollo.commons.server.spring.boot.starter.model.annotaion;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

import com.apoollo.commons.util.LangUtils;
import com.apoollo.commons.util.model.StorageUnit;
import com.apoollo.commons.util.request.context.RequestContext;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Getter;

/**
 * @author liuyulong
 * @since 2024-11-23
 */
@Aspect
public class RequestResourceAspect {

	private static final Logger LOGGER = LoggerFactory.getLogger(RequestResourceAspect.class);

	@Getter
	static class MultipartFileLoggingObject {
		private String name;
		private String originalFilename;
		private String contentType;
		private long size;
		private Double kb;

		/**
		 * @param name
		 * @param originalFilename
		 * @param contentType
		 * @param size
		 */
		public MultipartFileLoggingObject(MultipartFile multipartFile) {
			super();
			this.name = multipartFile.getName();
			this.originalFilename = multipartFile.getOriginalFilename();
			this.contentType = multipartFile.getContentType();
			this.size = multipartFile.getSize();
			this.kb = LangUtils.getStorageSize(StorageUnit.KILOBYTE, multipartFile.getSize());
		}

	}

	private Annotation getAnnotation(Annotation[][] paramAnnotations, int getIndex, Class<?> annotationClass) {
		if (null != paramAnnotations && paramAnnotations.length > getIndex) {
			Annotation[] annotations = paramAnnotations[getIndex];
			if (null != annotations) {

				return Arrays.stream(annotations).filter(annotation -> annotation.annotationType() == annotationClass)
						.findAny().orElse(null);
			}
		}
		return null;
	}

	private String toLogContent(Logable logable, Class<?> parameterClass, String parameterName, Object parameterValue) {
		String[] maskProperies = null == logable ? null : logable.maskProperies();
		String valueContent = null;
		if (null == parameterValue) {
			valueContent = "null";
		} else if (ArrayUtils.isNotEmpty(maskProperies) && null != parameterName && Arrays.stream(maskProperies)
				.filter(maskProperty -> parameterName.equals(maskProperty)).findAny().isPresent()) {
			valueContent = "******";
		} else if (parameterValue instanceof MultipartFile) {
			valueContent = LangUtils.toLoggingJsonString(new MultipartFileLoggingObject((MultipartFile) parameterValue),
					null);
		} else if (parameterValue instanceof MultipartFile[]) {
			valueContent = LangUtils.toLoggingJsonString(Arrays.stream((MultipartFile[]) parameterValue)
					.map(MultipartFileLoggingObject::new).collect(Collectors.toList()), null);
		} else {
			valueContent = LangUtils.toLoggingJsonString(parameterValue, maskProperies);
		}
		String logContent = null == parameterName ? valueContent : StringUtils.join(parameterName, " = ", valueContent);
		return logContent;
	}

	private void print(String logName, List<String> logContent) {
		if (CollectionUtils.isNotEmpty(logContent)) {
			String content = logContent.stream().collect(Collectors.joining(", "));
			LOGGER.info(StringUtils.join(logName, content));
		}
	}

	@Around(value = "@annotation(requestResource)")
	public Object advice(ProceedingJoinPoint point, RequestResource requestResource) throws Throwable {

		long startTime = System.currentTimeMillis();
		RequestContext requestContext = RequestContext.getRequired();

		MethodSignature signature = (MethodSignature) point.getSignature();
		Method method = signature.getMethod();
		Class<?>[] parameterTypes = signature.getParameterTypes();
		String[] parameterNames = signature.getParameterNames();
		Object[] parameterValues = point.getArgs();
		String requestResourceName = requestContext.getRequestResource().getName();

		// 打印入参
		String requestLogName = StringUtils.join("[", requestResourceName, "]入参: ");
		List<String> requestLogContent = null;
		if (ArrayUtils.isNotEmpty(parameterNames)) {
			Annotation[][] paramAnnotations = method.getParameterAnnotations();
			requestLogContent = IntStream//
					.range(0, parameterNames.length)//
					.mapToObj(i -> {
						Class<?> parameterType = parameterTypes[i];
						String parameterName = parameterNames[i];
						Object parameterValue = parameterValues[i];
						Logable logable = (Logable) getAnnotation(paramAnnotations, i, Logable.class);
						return new RequestResourceParameter(i, parameterType, parameterName, parameterValue, logable);
					})//
					.filter(parameter -> //
					(null == parameter.getLogable() || parameter.getLogable().enable())
							&& !(parameter.getParameterType() == HttpServletRequest.class
									|| parameter.getParameterType() == HttpServletResponse.class))
					.map(parameter -> {
						return toLogContent(parameter.getLogable(), parameter.getParameterType(),
								parameter.getParameterName(), parameter.getParameterValue());
					}).toList();
			print(requestLogName, requestLogContent);
		}

		Object object = point.proceed();

		// 出参打印日志
		String responseLogName = StringUtils.join("[", requestResourceName, "]出参: ");
		List<String> responseLogContent = null;
		if (method.getReturnType() != void.class && method.getReturnType() != Void.class) {
			Logable logable = method.getAnnotation(Logable.class);
			if (null == logable || logable.enable()) {
				responseLogContent = LangUtils.toList(toLogContent(logable, method.getReturnType(), null, object));
				print(responseLogName, responseLogContent);
			}
		}
		LOGGER.info(StringUtils.join("[", requestResourceName, "]", "耗时: ", System.currentTimeMillis() - startTime,
				"(ms)"));

		return object;
	}
}
