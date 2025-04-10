/**
 * 
 */
package com.apoollo.commons.server.spring.boot.starter.component.filter;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.util.StreamUtils;

import com.alibaba.fastjson2.util.IOUtils;
import com.apoollo.commons.server.spring.boot.starter.service.XssHandler;

import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

/**
 * liuyulong
 */
public class XssHttpServletRequestWrapper extends HttpServletRequestWrapper {

	private XssHandler xssHandler;
	
	/**
	 * @param request
	 * @param xssHandler
	 */
	public XssHttpServletRequestWrapper(HttpServletRequest request, XssHandler xssHandler) {
		super(request);
		this.xssHandler = xssHandler;
	}

	public Charset getCharset() {
		String characterEncodingName = getCharacterEncodingName();
		return Charset.forName(characterEncodingName);
	}

	public String getCharacterEncodingName() {
		String characterEncoding = super.getCharacterEncoding();
		if (StringUtils.isBlank(characterEncoding)) {
			characterEncoding = StandardCharsets.UTF_8.name();
		}
		return characterEncoding;

	}

	@Override
	public String getParameter(String name) {
		return xssHandler.escape(super.getParameter(name));
	}

	@Override
	public String[] getParameterValues(String name) {
		return xssHandler.escapes(super.getParameterValues(name));
	}

	@Override
	public Map<String, String[]> getParameterMap() {
		return xssHandler.escapes(super.getParameterMap());
	}

	@Override
	public ServletInputStream getInputStream() throws IOException {

		ServletInputStream servletInputStream = super.getInputStream();
		if (null != servletInputStream) {
			Charset charset = getCharset();
			String content = StreamUtils.copyToString(servletInputStream, charset);

			String contentType = super.getRequest().getContentType();
			String value = xssHandler.escapeBody(contentType, content);
			
			IOUtils.close(servletInputStream);
			servletInputStream = new ServletInputStream() {

				private ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(value.getBytes(charset));

				@Override
				public int read() throws IOException {
					return byteArrayInputStream.read();
				}

				@Override
				public void setReadListener(ReadListener readListener) {
					// throw new UnsupportedOperationException();
				}

				@Override
				public boolean isReady() {
					return true;
				}

				@Override
				public boolean isFinished() {
					return false;
				}
			};
		}
		return servletInputStream;
	}

	@Override
	public BufferedReader getReader() throws IOException {
		ServletInputStream servletInputStream = this.getInputStream();
		BufferedReader reader = null;
		if (null != servletInputStream) {
			reader = new BufferedReader(new InputStreamReader(servletInputStream, this.getCharacterEncodingName()));
		}
		return reader;
	}
}
