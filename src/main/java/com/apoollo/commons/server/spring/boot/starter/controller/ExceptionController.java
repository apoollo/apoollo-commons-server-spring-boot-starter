package com.apoollo.commons.server.spring.boot.starter.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.apoollo.commons.server.spring.boot.starter.model.Constants;

import jakarta.servlet.ServletRequest;

@RestController
public class ExceptionController {

	@RequestMapping(Constants.EXCEPTION_FORWARD_CONTROLLE_PATH)
	public void forward(ServletRequest request) throws Throwable {
		Throwable throwable = (Throwable) request.getAttribute(Constants.REQUEST_ATTRIBUTE_EXCEPTION);
		request.removeAttribute(Constants.REQUEST_ATTRIBUTE_EXCEPTION);
		throw throwable;
	}
}
