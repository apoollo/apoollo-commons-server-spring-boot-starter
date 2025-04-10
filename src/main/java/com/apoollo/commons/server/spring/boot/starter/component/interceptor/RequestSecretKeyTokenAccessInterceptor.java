/**
 * 
 */
package com.apoollo.commons.server.spring.boot.starter.component.interceptor;

import java.util.function.Function;

import org.apache.commons.lang3.Validate;

import com.apoollo.commons.server.spring.boot.starter.compatible.CompatibleUtils;
import com.apoollo.commons.server.spring.boot.starter.model.Constants;
import com.apoollo.commons.server.spring.boot.starter.properties.RequestSecretKeyTokenAccessInterceptorProperties;
import com.apoollo.commons.server.spring.boot.starter.service.AbstractInternalHandlerInterceptor;
import com.apoollo.commons.server.spring.boot.starter.service.Access;
import com.apoollo.commons.util.request.context.RequestContext;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * @author liuyulong
 */
public class RequestSecretKeyTokenAccessInterceptor extends AbstractInternalHandlerInterceptor {

    private Access<String> access;
    private String accessKeyProperty;
    private String secretKeyProperty;

    public RequestSecretKeyTokenAccessInterceptor(
            RequestSecretKeyTokenAccessInterceptorProperties requestSecretKeyTokenAccessInterceptor,
            Access<String> access) {
        super(requestSecretKeyTokenAccessInterceptor);
        this.access = access;
        this.accessKeyProperty = requestSecretKeyTokenAccessInterceptor.getAccessKeyProperty();
        this.secretKeyProperty = requestSecretKeyTokenAccessInterceptor.getSecretKeyProperty();
        Validate.notBlank(accessKeyProperty);
        Validate.notBlank(secretKeyProperty);
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {

        if (RequestContext.getRequired().getResourceAccessStrategyRequired()
                .crossRequestParameterSecretKeyTokenAccessInterceptor()) {
            access((accessKeyProperty) -> request.getParameter(accessKeyProperty),
                    (secretKeyProperty) -> request.getParameter(secretKeyProperty));
        } else if (RequestContext.getRequired().getResourceAccessStrategyRequired()
                .crossRequestHeaderSecretKeyTokenAccessInterceptor()) {
            access((accessKeyProperty) -> request.getHeader(accessKeyProperty),
                    (secretKeyProperty) -> request.getHeader(secretKeyProperty));
        }
        return true;
    }

    public void access(Function<String, String> accessKeyGetter, Function<String, String> secretKeyGetter) {
        String accessKeyValue = accessKeyGetter.apply(accessKeyProperty);
        String secretKeyValue = secretKeyGetter.apply(secretKeyProperty);
        access.access(CompatibleUtils.compatibleStringSpace(accessKeyValue),
                CompatibleUtils.compatibleStringSpace(secretKeyValue));
    }

    @Override
    public int getOrder() {
        return Constants.SECRET_KEY_TOKEN_ACCESS_INTERCEPTOR_ORDER;
    }

}
