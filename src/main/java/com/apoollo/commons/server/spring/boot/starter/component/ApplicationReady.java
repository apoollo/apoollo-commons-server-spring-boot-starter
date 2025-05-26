/**
 * 
 */
package com.apoollo.commons.server.spring.boot.starter.component;

import java.security.Security;
import java.util.List;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;

import com.apoollo.commons.server.spring.boot.starter.model.annotaion.RequestResourceRegister;
import com.apoollo.commons.server.spring.boot.starter.properties.CommonsServerProperties;
import com.apoollo.commons.server.spring.boot.starter.service.AfterApplicationReady;
import com.apoollo.commons.util.LangUtils;

/**
 * @author liuyulong
 * @since 2023年8月25日
 */
public class ApplicationReady implements ApplicationListener<ApplicationReadyEvent> {

	private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationReady.class);

	@Autowired
	private CommonsServerProperties commonsServerProperties;

	@Autowired(required = false)
	private List<AfterApplicationReady> afterApplicationReadys;

	// @Autowired(required = false)
	// private RedisProperties redisProperties;

	@Autowired
	private ApplicationContext applicationContext;

	@Override
	public void onApplicationEvent(ApplicationReadyEvent event) {
		Security.addProvider(new BouncyCastleProvider());
		new RequestResourceRegister(applicationContext, commonsServerProperties).regist();

		LangUtils.getStream(afterApplicationReadys)
				.forEach(afterApplicationReady -> afterApplicationReady.onApplicationEvent(event));

		/*
		 * LOGGER.info("公共服务配置文件参数:" + JSON.toJSONString(commonsServerProperties)); if
		 * (null != redisProperties) { LOGGER.info("Redis 配置文件:" +
		 * JSON.toJSONString(redisProperties)); }
		 */
		LOGGER.info("Java File Encodiing:" + System.getProperty("file.encoding"));
		LOGGER.info("OS Name:" + System.getProperty("os.name"));
		LOGGER.info("Java Version:" + System.getProperty("java.version"));
		// LOGGER.info("Self Version:" + Version.CURRENT_VERSION);
		LOGGER.info("Commons Server Spring Boot Stater Started <<<!!!");
	}

}
