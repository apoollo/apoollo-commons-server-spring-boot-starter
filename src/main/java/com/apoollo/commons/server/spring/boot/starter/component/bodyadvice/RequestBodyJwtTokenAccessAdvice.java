/**
 * 
 */
package com.apoollo.commons.server.spring.boot.starter.component.bodyadvice;

import java.io.IOException;
import java.lang.reflect.Type;

import org.springframework.core.MethodParameter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdviceAdapter;

import com.apoollo.commons.server.spring.boot.starter.model.Constants;
import com.apoollo.commons.server.spring.boot.starter.service.Access;
import com.apoollo.commons.server.spring.boot.starter.service.AuthorizationJwtTokenJwtTokenDecoder;
import com.apoollo.commons.util.JwtUtils.JwtToken;
import com.apoollo.commons.util.request.context.RequestContext;
import com.apoollo.commons.util.request.context.RequestResourceAccessStrategy;

/**
 * @author liuyulong
 */
@ControllerAdvice
public class RequestBodyJwtTokenAccessAdvice extends RequestBodyAdviceAdapter implements Ordered {

    private AuthorizationJwtTokenJwtTokenDecoder authorizationJwtTokenJwtTokenDecoder;
    private Access<JwtToken> access;

    public RequestBodyJwtTokenAccessAdvice(AuthorizationJwtTokenJwtTokenDecoder authorizationJwtTokenJwtTokenDecoder,
            Access<JwtToken> access) {
        super();
        this.access = access;
        this.authorizationJwtTokenJwtTokenDecoder = authorizationJwtTokenJwtTokenDecoder;
    }

    @Override
    public boolean supports(MethodParameter methodParameter, Type targetType,
            Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    @Override
    public HttpInputMessage beforeBodyRead(HttpInputMessage inputMessage, MethodParameter parameter, Type targetType,
            Class<? extends HttpMessageConverter<?>> converterType) throws IOException {
        return super.beforeBodyRead(inputMessage, parameter, targetType, converterType);
    }

    @Override
    public Object afterBodyRead(Object body, HttpInputMessage inputMessage, MethodParameter parameter, Type targetType,
            Class<? extends HttpMessageConverter<?>> converterType) {
        RequestResourceAccessStrategy requestResourceAccessStrategy = RequestContext.getRequired()
                .getResourceAccessStrategyRequired();
        if (requestResourceAccessStrategy.crossRequestBodyJwtTokenAccessAdvice()) {
            JwtToken jwtToken = authorizationJwtTokenJwtTokenDecoder
                    .decode(requestResourceAccessStrategy.getAuthenticationTokenFromRequestBody(body));
            access.access(jwtToken.getAccessKey(), jwtToken);

        }
        return super.afterBodyRead(body, inputMessage, parameter, targetType, converterType);
    }

    @Override
    public int getOrder() {
        return Constants.REQUEST_JWT_TOKEN_ACCESS_BODY_ADVICE_ORDER;
    }

}
