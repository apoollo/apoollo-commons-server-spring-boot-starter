/**
 * 
 */
package com.apoollo.commons.server.spring.boot.starter.model.annotaion;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.WordUtils;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.aop.support.AopUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.apoollo.commons.server.spring.boot.starter.properties.CommonsServerProperties;
import com.apoollo.commons.server.spring.boot.starter.properties.PathProperties;
import com.apoollo.commons.util.LangUtils;
import com.apoollo.commons.util.path.LeftFallingPathJoinner;
import com.apoollo.commons.util.request.context.access.core.DefaultRequestResource;
import com.apoollo.commons.util.web.spring.Instance;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * @author liuyulong
 */
public class RequestResourceRegister {

	private static final LeftFallingPathJoinner FALLING_PATH_JOINNER = new LeftFallingPathJoinner();

	private Instance instance;

	private PathProperties pathProperties;
	private List<DefaultRequestResource> requestResources;

	public RequestResourceRegister(Instance instance, CommonsServerProperties commonsServerProperties) {
		super();
		this.instance = instance;
		this.pathProperties = commonsServerProperties.getPath();
		this.requestResources = commonsServerProperties.getRbac().getRequestResources();
	}

	public void regist() {
		Map<String, Object> controllers = instance.getApplicationContext().getBeansWithAnnotation(Controller.class);
		regist(controllers);

	}

	public void regist(Map<String, Object> controllers) {
		LangUtils.getStream(controllers.entrySet()).forEach(entry -> {
			Object controller = entry.getValue();
			Class<?> controllerClass = null;
			if (AopUtils.isAopProxy(controller)) {
				controllerClass = AopProxyUtils.ultimateTargetClass(controller);
			} else {
				controllerClass = controller.getClass();
			}
			regist(controllerClass);
		});
	}

	public void regist(Class<?> controllerClass) {
		RequestMapping controllerRequestMapping = controllerClass.getAnnotation(RequestMapping.class);
		LangUtils.getStream(controllerClass.getDeclaredMethods())
				.map(method -> getRequestResourceMapping(controllerClass, controllerRequestMapping, method))
				.filter(Objects::nonNull)//
				.forEach(requestResourceMapping -> {
					if (requestResourceMapping.configed) {
						DefaultRequestResource requestResourceObject = requestResourceMapping
								.getRequestResourceObject();
						requestResources.add(requestResourceObject);
						pathProperties.getIncludePathPatterns().add(requestResourceObject.getRequestMappingPath());
					} /*
						 * else { if (null !=
						 * requestResourceMapping.getControllerMethodRequestMappingPath()) {
						 * pathProperties.getExcludePathPatterns()
						 * .add(requestResourceMapping.getControllerMethodRequestMappingPath()); } }
						 */
				});
	}

	public String getControllerMethodMappingPath(String controllerRequestMappingPath, Method method) {
		String requestMappingPath = null;
		String methodPath = null;
		RequestMapping methodRequestMapping = null;
		GetMapping getMapping = null;
		PostMapping postMapping = null;
		PutMapping putMapping = null;
		DeleteMapping deleteMapping = null;
		if (null != (methodRequestMapping = method.getAnnotation(RequestMapping.class))) {
			methodPath = getRequestMappingPath(methodRequestMapping);
		} else if (null != (getMapping = method.getAnnotation(GetMapping.class))) {
			methodPath = getGetMappingPath(getMapping);
		} else if (null != (postMapping = method.getAnnotation(PostMapping.class))) {
			methodPath = getPostMappingPath(postMapping);
		} else if (null != (putMapping = method.getAnnotation(PutMapping.class))) {
			methodPath = getPutMappingPath(putMapping);
		} else if (null != (deleteMapping = method.getAnnotation(DeleteMapping.class))) {
			methodPath = getDeleteMappingPath(deleteMapping);
		}
		if (null != methodPath) {
			requestMappingPath = FALLING_PATH_JOINNER.joinRootPath(controllerRequestMappingPath, methodPath);
		}
		return requestMappingPath;
	}

	public RequestResourceMapping getRequestResourceMapping(Class<?> controllerClass,
			RequestMapping controllerRequestMapping, Method method) {
		RequestResourceMapping requestResourceMapping = null;
		String controllerRequestMappingPath = getRequestMappingPath(controllerRequestMapping);
		if (method.isAnnotationPresent(RequestResource.class)) {
			RequestResource requestResourceAnnotaion = method.getAnnotation(RequestResource.class);
			String requestMappingPath = null;
			String annotionConfigMappingPath = requestResourceAnnotaion.requestMappingPath();

			if (StringUtils.isBlank(annotionConfigMappingPath)) {
				requestMappingPath = getControllerMethodMappingPath(controllerRequestMappingPath, method);
			} else {
				requestMappingPath = FALLING_PATH_JOINNER.joinRootPath(controllerRequestMappingPath,
						annotionConfigMappingPath);
			}
			if (StringUtils.isNotBlank(requestMappingPath)) {
				String resourcePin = requestResourceAnnotaion.resourcePin();
				if (StringUtils.isBlank(resourcePin)) {
					resourcePin = StringUtils.join(WordUtils.uncapitalize(controllerClass.getSimpleName()),
							WordUtils.capitalize(method.getName()));
				}
				requestResourceMapping = new RequestResourceMapping(true,
						getDefaultRequestResource(requestResourceAnnotaion, resourcePin, requestMappingPath),
						requestResourceAnnotaion, null);
			}
		} else {
			requestResourceMapping = new RequestResourceMapping(false, null, null,
					getControllerMethodMappingPath(controllerRequestMappingPath, method));
		}
		return requestResourceMapping;
	}

	public DefaultRequestResource getDefaultRequestResource(RequestResource requestResourceAnnotaion,
			String resourcePin, String requestMappingPath) {
		DefaultRequestResource requestResourceObject = new DefaultRequestResource();
		requestResourceObject.setResourcePin(requestResourceAnnotaion.resourcePin());
		requestResourceObject.setAccessKey(null);
		requestResourceObject.setEnableNonceLimiter(requestResourceAnnotaion.enableNonceLimiter());
		requestResourceObject.setNonceLimiterDuration(requestResourceAnnotaion.nonceLimiterDuration());
		requestResourceObject.setEnableSignatureLimiter(requestResourceAnnotaion.enableSignatureLimiter());
		requestResourceObject.setSignatureLimiterSecret(requestResourceAnnotaion.signatureLimiterSecret());
		requestResourceObject.setSignatureLimiterExcludeHeaderNames(
				toList(requestResourceAnnotaion.signatureLimiterExcludeHeaderNames()));
		requestResourceObject.setSignatureLimiterIncludeHeaderNames(
				toList(requestResourceAnnotaion.signatureLimiterIncludeHeaderNames()));
		requestResourceObject.setEnableCorsLimiter(requestResourceAnnotaion.enableCorsLimiter());
		requestResourceObject.setEnableIpLimiter(requestResourceAnnotaion.enableIpLimiter());
		requestResourceObject.setIpLimiterExcludes(toList(requestResourceAnnotaion.ipLimiterExcludes()));
		requestResourceObject.setIpLimiterIncludes(toList(requestResourceAnnotaion.ipLimiterIncludes()));

		requestResourceObject.setEnableRefererLimiter(requestResourceAnnotaion.enableRefererLimiter());
		requestResourceObject
				.setRefererLimiterIncludeReferers(toList(requestResourceAnnotaion.refererLimiterIncludeReferers()));

		requestResourceObject.setEnableSyncLimiter(requestResourceAnnotaion.enableSyncLimiter());
		requestResourceObject.setEnableFlowLimiter(requestResourceAnnotaion.enableFlowLimiter());
		requestResourceObject.setFlowLimiterLimitCount(requestResourceAnnotaion.flowLimiterLimitCount());
		requestResourceObject.setEnableCountLimiter(requestResourceAnnotaion.enableCountLimiter());
		requestResourceObject.setCountLimiterTimeUnitPattern(requestResourceAnnotaion.countLimiterTimeUnitPattern());
		requestResourceObject.setCountLimiterLimitCount(requestResourceAnnotaion.countLimiterLimitCount());
		requestResourceObject.setEnableContentEscape(requestResourceAnnotaion.enableContentEscape());
		requestResourceObject.setEnableResponseWrapper(requestResourceAnnotaion.enableResponseWrapper());

		if (requestResourceAnnotaion.enableContentEscape()) {
			requestResourceObject
					.setContentEscapeMethod(instance.getInstance(requestResourceAnnotaion.contentEscapeMethodClass()));
		}
		if (requestResourceAnnotaion.enableCorsLimiter()) {
			requestResourceObject.setCorsLimiterConfiguration(
					instance.getInstance(requestResourceAnnotaion.corsLimiterConfiguration()));
		}
		if (requestResourceAnnotaion.enableNonceLimiter()) {
			requestResourceObject
					.setNonceLimiterValidator(instance.getInstance(requestResourceAnnotaion.nonceLimiterValidator()));
		}
		if (requestResourceAnnotaion.enableResponseWrapper()) {
			requestResourceObject
					.setWrapResponseHandler(instance.getInstance(requestResourceAnnotaion.wrapResponseHandler()));
		}
		requestResourceObject.setEnableCapacity(requestResourceAnnotaion.enableCapacity());
		requestResourceObject.setEnable(requestResourceAnnotaion.enable());
		requestResourceObject.setName(LangUtils.defaultString(requestResourceAnnotaion.name(), resourcePin));
		requestResourceObject.setRequestMappingPath(requestMappingPath);
		requestResourceObject.setAccessStrategy(requestResourceAnnotaion.accessStrategy());
		requestResourceObject.setRoles(toList(requestResourceAnnotaion.roles()));

		return requestResourceObject;
	}

	private List<String> toList(String[] array) {
		return null == array ? null : Arrays.stream(array).toList();
	}

	public String getRequestMappingPath(RequestMapping mapping) {
		return null == mapping ? null
				: getRequestMappingPath(null == mapping.value() ? null : () -> mapping.value(),
						null == mapping.path() ? null : () -> mapping.path());
	}

	public String getGetMappingPath(GetMapping mapping) {
		return null == mapping ? null
				: getRequestMappingPath(null == mapping.value() ? null : () -> mapping.value(),
						null == mapping.path() ? null : () -> mapping.path());
	}

	public String getPostMappingPath(PostMapping mapping) {
		return null == mapping ? null
				: getRequestMappingPath(null == mapping.value() ? null : () -> mapping.value(),
						null == mapping.path() ? null : () -> mapping.path());
	}

	public String getPutMappingPath(PutMapping mapping) {
		return null == mapping ? null
				: getRequestMappingPath(null == mapping.value() ? null : () -> mapping.value(),
						null == mapping.path() ? null : () -> mapping.path());
	}

	public String getDeleteMappingPath(DeleteMapping mapping) {
		return null == mapping ? null
				: getRequestMappingPath(null == mapping.value() ? null : () -> mapping.value(),
						null == mapping.path() ? null : () -> mapping.path());
	}

	public String getRequestMappingPath(Supplier<String[]> values, Supplier<String[]> paths) {
		String requestMappingPath = null;
		if (null != values) {
			requestMappingPath = getArraysFirstValue(values.get());
		}
		if (null == requestMappingPath && null != paths) {
			requestMappingPath = getArraysFirstValue(paths.get());
		}
		return requestMappingPath;
	}

	public String getArraysFirstValue(String[]... arrays) {
		return LangUtils.getStream(arrays).flatMap(LangUtils::getStream).filter(StringUtils::isNotBlank).findFirst()
				.orElse(null);
	}

	@Getter
	@Setter
	@AllArgsConstructor
	public static class RequestResourceMapping {
		private Boolean configed;
		private DefaultRequestResource requestResourceObject;
		private RequestResource requestResourceAnnotaion;
		private String controllerMethodRequestMappingPath;

	}

}
