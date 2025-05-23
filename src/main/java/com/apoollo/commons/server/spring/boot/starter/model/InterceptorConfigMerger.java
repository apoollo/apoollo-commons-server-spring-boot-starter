package com.apoollo.commons.server.spring.boot.starter.model;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.apoollo.commons.server.spring.boot.starter.properties.PathProperties;
import com.apoollo.commons.util.LangUtils;

import lombok.Getter;

/**
 * @author liuyulong
 */
@Getter
public class InterceptorConfigMerger {

	private List<String> pathPatterns = new ArrayList<>();
	private List<String> excludePathPatterns = new ArrayList<>();

	public InterceptorConfigMerger(PathProperties... propertiesArray) {

		LangUtils.getStream(propertiesArray).forEach(properties -> {
			LangUtils.getStream(properties.getIncludePathPatterns()).forEach(pathPatterns::add);
			LangUtils.getStream(properties.getExcludePathPatterns()).forEach(excludePathPatterns::add);
		});
		pathPatterns = LangUtils.getStream(pathPatterns).distinct().collect(Collectors.toList());
		excludePathPatterns = LangUtils.getStream(excludePathPatterns).distinct().collect(Collectors.toList());
	}
}
