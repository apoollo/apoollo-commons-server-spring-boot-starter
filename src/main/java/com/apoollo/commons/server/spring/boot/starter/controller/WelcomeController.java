/**
 * 
 */
package com.apoollo.commons.server.spring.boot.starter.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.apoollo.commons.server.spring.boot.starter.model.annotaion.RequestResource;
import com.apoollo.commons.util.request.context.core.AccessStrategy;

/**
 * @author liuyulong
 */
@RestController
@RequestMapping("/welcome")
public class WelcomeController {

	@GetMapping("/public")
	@RequestResource(name = "欢迎", accessStrategy = AccessStrategy.PUBLIC)
	public String welcome() {
		return "welcome!";
	}

}
