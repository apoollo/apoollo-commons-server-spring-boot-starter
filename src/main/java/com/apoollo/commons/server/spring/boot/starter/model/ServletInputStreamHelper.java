/**
 * 
 */
package com.apoollo.commons.server.spring.boot.starter.model;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.function.Function;

import org.apache.commons.lang3.StringUtils;
import org.springframework.util.StreamUtils;
import org.springframework.web.util.ContentCachingRequestWrapper;

import com.apoollo.commons.util.request.context.RequestContext;

import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.ServletRequest;

/**
 * @author liuyulong
 * @since 2025-05-19
 */
public class ServletInputStreamHelper {

	public static Charset getCharset(ServletRequest request) {
		String characterEncodingName = getCharacterEncodingName(request);
		return Charset.forName(characterEncodingName);
	}

	public static String getCharacterEncodingName(ServletRequest request) {
		String characterEncoding = request.getCharacterEncoding();
		if (StringUtils.isBlank(characterEncoding)) {
			characterEncoding = StandardCharsets.UTF_8.name();
		}
		return characterEncoding;

	}

	public static byte[] getBodyByteArray(ServletRequest request) throws IOException {
		byte[] array = null;
		try (ServletInputStream servletInputStream = request.getInputStream()) {
			if (null != servletInputStream) {
				array = StreamUtils.copyToByteArray(servletInputStream);
			}
		}
		return array;
	}

	public static byte[] getCacheBodyByteArray(ServletRequest request) throws IOException {
		byte[] array = null;
		RequestContext requestContext = RequestContext.get();
		if (null != requestContext) {
			array = requestContext.getRequestBody();
		}
		if (null == array) {
			if (request instanceof ContentCachingRequestWrapper) {
				ContentCachingRequestWrapper contentCachingRequestWrapper = (ContentCachingRequestWrapper) request;
				array = contentCachingRequestWrapper.getContentAsByteArray();
			}
		}
		return array;

	}

	public static ServletInputStream getCachingServletInputStream(ServletRequest request, Function<String, String> bodyMap)
			throws IOException {
		ServletInputStream servletInputStream = null;
		byte[] bytes = getCacheBodyByteArray(request);
		if (null == bytes) {
			servletInputStream = new EmptyServletInputStream();
		} else {
			Charset charset = getCharset(request);
			String content = new String(bytes, charset);
			String value = bodyMap.apply(content);
			servletInputStream = new ByteArrayServletInputStream(value.getBytes(charset));
		}
		return servletInputStream;
	}

	public static class ByteArrayServletInputStream extends ServletInputStream {

		private final ByteArrayInputStream inputStream;

		public ByteArrayServletInputStream(byte[] data) {
			this.inputStream = new ByteArrayInputStream(data);
		}

		@Override
		public int read() throws IOException {
			return inputStream.read();
		}

		@Override
		public boolean isReady() {
			return true;
		}

		@Override
		public boolean isFinished() {
			return inputStream.available() == 0;
		}

		@Override
		public void setReadListener(ReadListener listener) {
		}
	}

	public static class EmptyServletInputStream extends ServletInputStream {

		@Override
		public int read() {
			return -1;
		}

		@Override
		public boolean isReady() {
			return true;
		}

		@Override
		public boolean isFinished() {
			return true;
		}

		@Override
		public void setReadListener(ReadListener listener) {
		}
	}
}
