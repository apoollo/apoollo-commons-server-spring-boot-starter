/**
 * 
 */
package com.apoollo.commons.server.spring.boot.starter.service;

import com.apoollo.commons.util.JwtUtils.JwtToken;

/**
 * @author liuyulong
 */
public interface AuthorizationJwtTokenJwtTokenDecoder {

    public JwtToken decode(String authorizationJwtToken);

}
