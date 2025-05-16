/**
 * 
 */
package com.apoollo.commons.server.spring.boot.starter.component.bodyadvice;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.MethodParameter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdviceAdapter;

import com.apoollo.commons.server.spring.boot.starter.model.Constants;
import com.apoollo.commons.server.spring.boot.starter.properties.RequestBodyDigestValidateAdviceProperties;
import com.apoollo.commons.util.Base64Utils;
import com.apoollo.commons.util.LangUtils;
import com.apoollo.commons.util.Md5Utils;
import com.apoollo.commons.util.crypto.symmetric.DESede;
import com.apoollo.commons.util.crypto.symmetric.SymmetricEncryption;
import com.apoollo.commons.util.exception.AppException;
import com.apoollo.commons.util.exception.AppIllegalArgumentException;
import com.apoollo.commons.util.request.context.RequestContext;
import com.apoollo.commons.util.request.context.RequestResource;

import jakarta.servlet.http.HttpServletRequest;

/**
 * @author liuyulong
 * @since 2023年9月22日
 */
@ControllerAdvice
public class RequestBodyDigestValidateAdvice extends RequestBodyAdviceAdapter implements Ordered {

	private static final Logger LOGGER = LoggerFactory.getLogger(RequestBodyDigestValidateAdvice.class);

	private SymmetricEncryption symmetricEncryption = new DESede();

	private String secret;

	public RequestBodyDigestValidateAdvice(
			RequestBodyDigestValidateAdviceProperties requestBodyDigestValidateAdviceProperties) {
		super();
		this.secret = requestBodyDigestValidateAdviceProperties.getSecret();
	}

	@Override
	public boolean supports(MethodParameter methodParameter, Type targetType,
			Class<? extends HttpMessageConverter<?>> converterType) {
		return true;
	}

	@Override
	public HttpInputMessage beforeBodyRead(HttpInputMessage inputMessage, MethodParameter parameter, Type targetType,
			Class<? extends HttpMessageConverter<?>> converterType) throws IOException {
		RequestContext requestContext = RequestContext.getRequired();
		RequestResource requestResource = requestContext.getRequestResource();

		if (requestContext.getResourceAccessStrategyRequired().crossRequestBodyDigestValidateAdvice()
				&& BooleanUtils.isTrue(requestResource.getEnableBodyDigestValidate())) {

			RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
			if (requestAttributes instanceof ServletRequestAttributes) {
				HttpServletRequest httpServletRequest = ((ServletRequestAttributes) requestAttributes).getRequest();
				String encryptDigest = httpServletRequest.getHeader("Digest");

				if (StringUtils.isBlank(encryptDigest)) {
					throw new AppIllegalArgumentException("header [Digest] must not be null");
				}
				String secret = LangUtils.defaultString(requestResource.getBodyDigestSecret(), this.secret);

				String decryptDigest;
				try {
					byte[] decrypted = symmetricEncryption.decrypt(Base64Utils.decode(secret),
							Base64Utils.decode(encryptDigest));
					decryptDigest = new String(decrypted, StandardCharsets.UTF_8);
				} catch (Exception e) {
					LOGGER.error("Digest decrypt  failed:", e);
					throw new AppIllegalArgumentException("Digest decrypt  failed");
				}

				ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
				IOUtils.copy(inputMessage.getBody(), byteArrayOutputStream);
				byte[] body = byteArrayOutputStream.toByteArray();

				if (!Md5Utils.compare(decryptDigest, body)) {
					throw new AppIllegalArgumentException("Digest compared false ,body probably has been modified");
				}

				return new HttpInputMessage() {

					@Override
					public HttpHeaders getHeaders() {
						return inputMessage.getHeaders();
					}

					@Override
					public InputStream getBody() throws IOException {
						return new ByteArrayInputStream(body);
					}
				};

			} else {
				throw new AppException(
						"RequestDigestValidateBodyAdvice requestAttributes must is org.springframework.web.context.request.ServletRequestAttributes");
			}
		}

		return super.beforeBodyRead(inputMessage, parameter, targetType, converterType);
	}

	@Override
	public int getOrder() {
		return Constants.REQUEST_DIGEST_VALIDATE_BODY_ADVICE_ORDER;
	}

}
