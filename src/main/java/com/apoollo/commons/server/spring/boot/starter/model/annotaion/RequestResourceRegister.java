/**
 * 
 */
package com.apoollo.commons.server.spring.boot.starter.model.annotaion;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.WordUtils;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.aop.support.AopUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.apoollo.commons.server.spring.boot.starter.properties.CommonsServerProperties;
import com.apoollo.commons.server.spring.boot.starter.properties.InterceptorCommonsProperties;
import com.apoollo.commons.util.LangUtils;
import com.apoollo.commons.util.path.LeftFallingPathJoinner;
import com.apoollo.commons.util.request.context.def.DefaultRequestResource;
import com.apoollo.commons.util.request.context.def.ResourceType;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * @author liuyulong
 */
public class RequestResourceRegister {

	private static final LeftFallingPathJoinner FALLING_PATH_JOINNER = new LeftFallingPathJoinner();

	private ApplicationContext applicationContext;

	private InterceptorCommonsProperties commonsProperties;
	private List<DefaultRequestResource> requestResources;

	public RequestResourceRegister(ApplicationContext applicationContext,
			CommonsServerProperties commonsServerProperties) {
		super();
		this.applicationContext = applicationContext;
		this.commonsProperties = commonsServerProperties.getCommonsIntercetptor();
		this.requestResources = commonsServerProperties.getRbac().getRequestResources();
	}

	public void regist() {
		Map<String, Object> controllers = applicationContext.getBeansWithAnnotation(Controller.class);
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
					DefaultRequestResource requestResourceObject = requestResourceMapping.getRequestResourceObject();
					if (ResourceType.STATIC == requestResourceMapping.getRequestResourceAnnotaion().resourceType()) {
						requestResources.add(requestResourceObject);
					}
					commonsProperties.getPathPatterns().add(requestResourceObject.getRequestMappingPath());
				});
	}

	public RequestResourceMapping getRequestResourceMapping(Class<?> controllerClass,
			RequestMapping controllerRequestMapping, Method method) {
		RequestResourceMapping requestResourceMapping = null;
		if (method.isAnnotationPresent(RequestResource.class)) {

			RequestResource requestResourceAnnotaion = method.getAnnotation(RequestResource.class);

			String requestMappingPath = requestResourceAnnotaion.requestMappingPath();
			if (StringUtils.isBlank(requestMappingPath)) {
				String controllerPath = getRequestMappingPath(controllerRequestMapping);
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
					requestMappingPath = FALLING_PATH_JOINNER.joinRootPath(controllerPath, methodPath);
				}
			}

			if (StringUtils.isNotBlank(requestMappingPath)) {
				String resourcePin = requestResourceAnnotaion.resourcePin();
				if (StringUtils.isBlank(resourcePin)) {
					resourcePin = StringUtils.join(WordUtils.uncapitalize(controllerClass.getSimpleName()),
							WordUtils.capitalize(method.getName()));
				}

				DefaultRequestResource requestResourceObject = new DefaultRequestResource();
				requestResourceObject.setEnable(requestResourceAnnotaion.enable());
				requestResourceObject.setAccessStrategy(requestResourceAnnotaion.accessStrategy().getAccessStrategyPin());
				requestResourceObject.setCustomizeAccessStrategyClass(requestResourceAnnotaion.customizeAccessStrategyClass());
				requestResourceObject.setResourcePin(resourcePin);
				requestResourceObject.setName(LangUtils.defaultString(requestResourceAnnotaion.name(), resourcePin));
				requestResourceObject.setRequestMappingPath(requestMappingPath);
				requestResourceObject.setLimtPlatformQps(requestResourceAnnotaion.limtPlatformQps());
				requestResourceObject.setLimtUserQps(requestResourceAnnotaion.limtUserQps());
				requestResourceObject.setRoles(requestResourceAnnotaion.roles());
				requestResourceObject.setEnableSync(requestResourceAnnotaion.enableSync());
				requestResourceMapping = new RequestResourceMapping(requestResourceObject, requestResourceAnnotaion);
			}

		}
		return requestResourceMapping;
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
		private DefaultRequestResource requestResourceObject;
		private RequestResource requestResourceAnnotaion;

	}

}
