/**
 * 
 */
package com.apoollo.commons.server.spring.boot.starter.properties;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

/**
 * liuyulong
 */
@Getter
@Setter
public class FilterProperties extends EnablePorperties {

	private List<String> pathPatterns;
}
