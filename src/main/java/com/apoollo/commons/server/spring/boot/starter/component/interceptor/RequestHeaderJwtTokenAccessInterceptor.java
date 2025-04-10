/**
 * 
 */
package com.apoollo.commons.server.spring.boot.starter.component.interceptor;

import com.apoollo.commons.server.spring.boot.starter.model.Constants;
import com.apoollo.commons.server.spring.boot.starter.properties.InterceptorCommonsProperties;
import com.apoollo.commons.server.spring.boot.starter.service.AbstractInternalHandlerInterceptor;
import com.apoollo.commons.server.spring.boot.starter.service.Access;
import com.apoollo.commons.server.spring.boot.starter.service.AuthorizationJwtTokenJwtTokenDecoder;
import com.apoollo.commons.server.spring.boot.starter.service.impl.JwtAuthorizationRenewal;
import com.apoollo.commons.util.JwtUtils;
import com.apoollo.commons.util.JwtUtils.JwtToken;
import com.apoollo.commons.util.request.context.RequestContext;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * @author liuyulong
 */
public class RequestHeaderJwtTokenAccessInterceptor extends AbstractInternalHandlerInterceptor {

    private AuthorizationJwtTokenJwtTokenDecoder authorizationJwtTokenJwtTokenDecoder;
    private Access<JwtToken> access;
    private JwtAuthorizationRenewal authorizationRenewal;

    public RequestHeaderJwtTokenAccessInterceptor(InterceptorCommonsProperties interceptorProperties,
            AuthorizationJwtTokenJwtTokenDecoder authorizationJwtTokenJwtTokenDecoder, Access<JwtToken> access,
            JwtAuthorizationRenewal authorizationRenewal) {
        super(interceptorProperties);
        this.authorizationJwtTokenJwtTokenDecoder = authorizationJwtTokenJwtTokenDecoder;
        this.access = access;
        this.authorizationRenewal = authorizationRenewal;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {

        if (RequestContext.getRequired().getResourceAccessStrategyRequired()
                .crossRequestHeaderJwtTokenAccessInterceptor()) {

            JwtToken jwtToken = authorizationJwtTokenJwtTokenDecoder
                    .decode(request.getHeader(JwtUtils.HEADER_AUTHORIZATIONE));

            access.access(jwtToken.getAccessKey(), jwtToken);

            authorizationRenewal.renewal(RequestContext.getRequired().getUser(), jwtToken, (renewal) -> {
                response.setHeader("x-renewal-authorization", renewal.getRenewalAuthorizationJwtToken());
            });
        }
        return true;
    }

    @Override
    public int getOrder() {
        return Constants.JWT_TOKEN_ACCESS_INTERCEPTOR_ORDER;
    }

}
