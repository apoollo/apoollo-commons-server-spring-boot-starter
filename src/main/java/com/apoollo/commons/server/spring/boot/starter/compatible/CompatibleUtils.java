/**
 * 
 */
package com.apoollo.commons.server.spring.boot.starter.compatible;

import com.apoollo.commons.server.spring.boot.starter.model.Version;

import jakarta.servlet.http.HttpServletResponse;

/**
 * @author liuyulong
 * 
 *         完全是为了兼容查询平台之前的老逻辑
 */
public class CompatibleUtils {

	public static String compatibleStringSpace(String input) {
		// 用于用户的appId，appKey
		// return null == input ? null : input.replace(" ", "");
		return input;
	}

	public static void compatibleResponse(HttpServletResponse response, String requestId) {
		response.setHeader("version", Version.CURRENT_VERSION);
	}
}
