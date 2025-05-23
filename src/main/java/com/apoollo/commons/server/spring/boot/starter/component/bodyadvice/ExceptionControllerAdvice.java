/**
 * 
 */
package com.apoollo.commons.server.spring.boot.starter.component.bodyadvice;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import com.apoollo.commons.util.LangUtils;
import com.apoollo.commons.util.exception.AppExceedingDailyMaximumUseTimesLimitException;
import com.apoollo.commons.util.exception.AppException;
import com.apoollo.commons.util.exception.AppForbbidenException;
import com.apoollo.commons.util.exception.AppHttpCodeMessageException;
import com.apoollo.commons.util.exception.AppIllegalArgumentException;
import com.apoollo.commons.util.exception.AppNoRequestResourceException;
import com.apoollo.commons.util.exception.AppRequestTimeoutLimitException;
import com.apoollo.commons.util.exception.AppServerOverloadedException;
import com.apoollo.commons.util.exception.detailed.AccessKeyEmptyException;
import com.apoollo.commons.util.exception.detailed.IpLimterException;
import com.apoollo.commons.util.exception.detailed.TimeoutIllegalArgumentException;
import com.apoollo.commons.util.exception.detailed.TokenEmptyExcetion;
import com.apoollo.commons.util.request.context.HttpCodeName;
import com.apoollo.commons.util.request.context.HttpCodeNameHandler;
import com.apoollo.commons.util.request.context.RequestContext;
import com.apoollo.commons.util.request.context.Response;
import com.apoollo.commons.util.request.context.def.DefaultHttpCodeNameHandler;

/**
 * @author liuyulong
 * @since 2023年7月31日
 */
@ControllerAdvice
public class ExceptionControllerAdvice {

	private static final Logger LOGGER = LoggerFactory.getLogger(ExceptionControllerAdvice.class);

	@ResponseBody
	@ExceptionHandler(NoHandlerFoundException.class)
	public <T> ResponseEntity<Response<T>> handleException(NoHandlerFoundException e) {
		return response(HttpCodeNameHandler::getNoHandlerFound, null, e, e.getMessage());
	}

	@ResponseBody
	@ExceptionHandler(NoResourceFoundException.class)
	public <T> ResponseEntity<Response<T>> handleException(NoResourceFoundException e) {
		return response(HttpCodeNameHandler::getNoHandlerFound, null, e, e.getMessage());
	}

	@ResponseBody
	@ExceptionHandler(AppNoRequestResourceException.class)
	public <T> ResponseEntity<Response<T>> handleException(AppNoRequestResourceException e) {
		return response(HttpCodeNameHandler::getNoHandlerFound, null, e, e.getMessage());
	}

	@ResponseBody
	@ExceptionHandler(HttpRequestMethodNotSupportedException.class)
	public <T> ResponseEntity<Response<T>> handleException(HttpRequestMethodNotSupportedException e) {
		return response(HttpCodeNameHandler::getIllegalArgument, null, e, e.getMessage());
	}

	@ResponseBody
	@ExceptionHandler(HttpMediaTypeNotSupportedException.class)
	public <T> ResponseEntity<Response<T>> handleException(HttpMediaTypeNotSupportedException e) {
		return response(HttpCodeNameHandler::getIllegalArgument, null, e, e.getMessage());
	}

	@ResponseBody
	@ExceptionHandler(HttpMessageNotReadableException.class)
	public <T> ResponseEntity<Response<T>> handleException(HttpMessageNotReadableException e) {
		return response(HttpCodeNameHandler::getIllegalArgument, null, e, "Http Message Not Readable");
	}

	@ResponseBody
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public <T> ResponseEntity<Response<T>> handleException(MethodArgumentNotValidException e) {
		List<String> filedList = null;
		BindingResult bindingResult = e.getBindingResult();
		if (null != bindingResult) {
			filedList = LangUtils.getStream(bindingResult.getAllErrors()).map(objectError -> {
				String filed = null;
				if (objectError instanceof FieldError) {
					FieldError fieldError = (FieldError) objectError;
					filed = fieldError.getField();
				}
				return filed;
			}).filter(StringUtils::isNotBlank).collect(Collectors.toList());
		}
		return response(HttpCodeNameHandler::getIllegalArgument, filedList.toArray(), e, null);
	}

	@ResponseBody
	@ExceptionHandler(MaxUploadSizeExceededException.class)
	public <T> ResponseEntity<Response<T>> handleException(MaxUploadSizeExceededException e) {
		return response(HttpCodeNameHandler::getAppMaxUploadSizeExceededException, null, e, e.getMessage());
	}

	@ResponseBody
	@ExceptionHandler(AppForbbidenException.class)
	public <T> ResponseEntity<Response<T>> handleException(AppForbbidenException e) {
		return response(HttpCodeNameHandler::getForbbiden, null, e, e.getMessage());
	}

	@ResponseBody
	@ExceptionHandler(AppExceedingDailyMaximumUseTimesLimitException.class)
	public <T> ResponseEntity<Response<T>> handleException(AppExceedingDailyMaximumUseTimesLimitException e) {
		return response(HttpCodeNameHandler::getExceedingDailyMaximumUseTimesLimit, null, e, e.getMessage());
	}

	@ResponseBody
	@ExceptionHandler(AppIllegalArgumentException.class)
	public <T> ResponseEntity<Response<T>> handleException(AppIllegalArgumentException e) {
		return response(HttpCodeNameHandler::getIllegalArgument, null, e, e.getMessage());
	}

	@ResponseBody
	@ExceptionHandler(TimeoutIllegalArgumentException.class)
	public <T> ResponseEntity<Response<T>> handleException(TimeoutIllegalArgumentException e) {
		return response(HttpCodeNameHandler::getTimeoutIllegalArgument, null, e, e.getMessage());
	}

	@ResponseBody
	@ExceptionHandler(AccessKeyEmptyException.class)
	public <T> ResponseEntity<Response<T>> handleException(AccessKeyEmptyException e) {
		return response(HttpCodeNameHandler::getAccessKeyEmpty, null, e, e.getMessage());

	}

	@ResponseBody
	@ExceptionHandler(TokenEmptyExcetion.class)
	public <T> ResponseEntity<Response<T>> handleException(TokenEmptyExcetion e) {
		return response(HttpCodeNameHandler::getTokenEmpty, null, e, e.getMessage());
	}

	@ResponseBody
	@ExceptionHandler(IpLimterException.class)
	public <T> ResponseEntity<Response<T>> handleException(IpLimterException e) {
		return response(HttpCodeNameHandler::getIpLimit, null, e, e.getMessage());
	}

	@ResponseBody
	@ExceptionHandler(AppHttpCodeMessageException.class)
	public <T> ResponseEntity<Response<T>> handleException(AppHttpCodeMessageException e) {
		return response((httpCodeNameHandler) -> e.getHttpCodeName(), e.getMessageCompileArgs(), e, null);
	}

	@ResponseBody
	@ExceptionHandler(AppRequestTimeoutLimitException.class)
	public <T> ResponseEntity<Response<T>> handleException(AppRequestTimeoutLimitException e) {
		return response(HttpCodeNameHandler::getTimeoutLimit, null, e, e.getMessage());
	}

	@ResponseBody
	@ExceptionHandler(AppServerOverloadedException.class)
	public <T> ResponseEntity<Response<T>> handleException(AppServerOverloadedException e) {
		return response(HttpCodeNameHandler::getServerOverloaded, null, e, e.getMessage());
	}

	@ResponseBody
	@ExceptionHandler(AppException.class)
	public <T> ResponseEntity<Response<T>> handleException(AppException e) {
		return response(HttpCodeNameHandler::getServerError, null, e, e.getMessage());
	}

	@ResponseBody
	@ExceptionHandler(Exception.class)
	public <T> ResponseEntity<Response<T>> handleException(Exception e) {
		return response(HttpCodeNameHandler::getServerError, null, e, null);
	}

	private <T> ResponseEntity<Response<T>> response(
			Function<HttpCodeNameHandler, HttpCodeName<String, String>> httpCodeGetter, Object[] messageCompileArgs,
			Exception e, String appendMessage) {

		RequestContext requestContext = RequestContext.get();
		HttpCodeNameHandler httpCodeNameHandler = null;
		T data = null;
		if (null != requestContext) {
			httpCodeNameHandler = requestContext.getRequestResource().getHttpCodeNameHandler();
			data = requestContext.getHintOfExceptionCatchedData();
		} else {
			httpCodeNameHandler = new DefaultHttpCodeNameHandler();
		}
		HttpCodeName<String, String> codeMessage = httpCodeGetter.apply(httpCodeNameHandler);

		LOGGER.error(HttpCodeNameHandler.getDefaultMessage(codeMessage.getName(), messageCompileArgs, appendMessage),
				e);

		return new ResponseEntity<>(
				httpCodeNameHandler.getResponse(codeMessage, messageCompileArgs, appendMessage, data),
				HttpStatusCode.valueOf(codeMessage.getHttpCode()));
	}
}
