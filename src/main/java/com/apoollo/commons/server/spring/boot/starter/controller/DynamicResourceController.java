/**
 * 
 */
package com.apoollo.commons.server.spring.boot.starter.controller;

import java.io.IOException;
import java.time.Duration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.apoollo.commons.server.spring.boot.starter.model.annotaion.RequestResource;
import com.apoollo.commons.util.request.context.def.AccessStrategy;
import com.apoollo.commons.util.web.captcha.CaptchaService;

import jakarta.servlet.http.HttpServletResponse;

/**
 * @author liuyulong
 */
@RestController
@RequestMapping("/dynamic/resource")
public class DynamicResourceController {

	@Autowired
	private CaptchaService captchaService;

	@GetMapping(value = "/public/get/captcha/stream")
	@RequestResource(name = "获取验证码图像流", accessStrategy = AccessStrategy.PUBLIC_REQUEST, limtPlatformQps = 6, enableSync = true)
	public void writeCaptcha(HttpServletResponse response, String token) throws IOException {
		captchaService.writeCaptchaImage(response, token, Duration.ofSeconds(60));
	}

}
