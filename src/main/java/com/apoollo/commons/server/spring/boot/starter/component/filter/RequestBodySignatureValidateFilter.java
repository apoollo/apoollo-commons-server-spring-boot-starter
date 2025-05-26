/**
 * 
 */
package com.apoollo.commons.server.spring.boot.starter.component.filter;

import java.io.IOException;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.apoollo.commons.server.spring.boot.starter.model.Constants;
import com.apoollo.commons.server.spring.boot.starter.model.ServletInputStreamHelper;
import com.apoollo.commons.server.spring.boot.starter.properties.PathProperties;
import com.apoollo.commons.util.Md5Utils;
import com.apoollo.commons.util.exception.AppIllegalArgumentException;
import com.apoollo.commons.util.request.context.RequestContext;
import com.apoollo.commons.util.request.context.RequestResource;
import com.apoollo.commons.util.request.context.SignatureDecryptor;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * @author liuyulong
 * @since 2025-05-19
 */
public class RequestBodySignatureValidateFilter extends AbstractSecureFilter {

	private static final Logger LOGGER = LoggerFactory.getLogger(RequestBodySignatureValidateFilter.class);

	private SignatureDecryptor signatureDecryptor;

	public RequestBodySignatureValidateFilter(PathProperties pathProperties, SignatureDecryptor signatureDecryptor) {
		super(pathProperties);
		this.signatureDecryptor = signatureDecryptor;
	}

	@Override
	public void doSecureFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws IOException, ServletException {

		RequestContext requestContext = RequestContext.getRequired();
		RequestResource requestResource = requestContext.getRequestResource();

		if (BooleanUtils.isTrue(requestResource.getEnableBodySignatureValidate())) {
			String signatureDigest = request.getHeader(Constants.REQUEST_SIGNATURE);
			if (StringUtils.isBlank(signatureDigest)) {
				throw new AppIllegalArgumentException("header [" + Constants.REQUEST_SIGNATURE + "] must not be null");
			}
			SignatureDecryptor signatureDecryptor = requestResource.getBodySignatureDecyptor();
			if (null == signatureDecryptor) {
				signatureDecryptor = this.signatureDecryptor;
			}
			byte[] digest = signatureDecryptor.decrypt(signatureDigest,
					requestResource.getBodySignatureDecyptorSecret());
			if (!Md5Utils.compare(digest, ServletInputStreamHelper.getBodyByteArray(request))) {
				throw new AppIllegalArgumentException("signature compared false ,body probably has been modified");
			}
			LOGGER.info("body signature validate accessed");

		}
		chain.doFilter(request, response);
	}


}
