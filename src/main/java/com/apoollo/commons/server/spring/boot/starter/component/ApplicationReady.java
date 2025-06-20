/**
 * 
 */
package com.apoollo.commons.server.spring.boot.starter.component;

import java.security.Security;
import java.util.Comparator;
import java.util.List;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;

import com.apoollo.commons.server.spring.boot.starter.model.Constants;
import com.apoollo.commons.server.spring.boot.starter.model.annotaion.RequestResourceRegister;
import com.apoollo.commons.server.spring.boot.starter.properties.CommonsServerProperties;
import com.apoollo.commons.server.spring.boot.starter.properties.PathProperties;
import com.apoollo.commons.util.LangUtils;
import com.apoollo.commons.util.request.context.Instances;

/**
 * @author liuyulong
 * @since 2023年8月25日
 */
public class ApplicationReady implements ApplicationListener<ApplicationReadyEvent> {

	private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationReady.class);

	private List<AfterApplicationReady> afterApplicationReadys;
	private Instances instances;
	private PathProperties pathProperties;

	public ApplicationReady(List<AfterApplicationReady> afterApplicationReadys, Instances instances,
			CommonsServerProperties commonsServerProperties) {
		super();
		this.afterApplicationReadys = afterApplicationReadys;
		this.instances = instances;
		this.pathProperties = commonsServerProperties.getPath();
	}

	@Override
	public void onApplicationEvent(ApplicationReadyEvent event) {
		Security.addProvider(new BouncyCastleProvider());
		new RequestResourceRegister(instances, pathProperties, Constants.REQUEST_RESOURCES).regist();

		LangUtils.getStream(afterApplicationReadys).sorted(Comparator.comparingInt(AfterApplicationReady::getOrder))
				.forEach(afterApplicationReady -> afterApplicationReady.onApplicationEvent(event));

		LOGGER.info("Java File Encodiing:" + System.getProperty("file.encoding"));
		LOGGER.info("OS Name:" + System.getProperty("os.name"));
		LOGGER.info("Java Version:" + System.getProperty("java.version"));
		LOGGER.info("Commons Server Spring Boot Stater Started <<<!!!");
	}

}
