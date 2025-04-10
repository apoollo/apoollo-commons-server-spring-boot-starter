/**
 * 
 */
package com.apoollo.commons.server.spring.boot.starter.service.impl;

import java.util.function.Consumer;

import org.apache.commons.lang3.BooleanUtils;

import com.apoollo.commons.server.spring.boot.starter.service.AuthorizationRenewal;
import com.apoollo.commons.server.spring.boot.starter.service.UserManager;
import com.apoollo.commons.util.JwtUtils;
import com.apoollo.commons.util.JwtUtils.JwtToken;
import com.apoollo.commons.util.JwtUtils.Renewal;
import com.apoollo.commons.util.request.context.User;

/**
 * @author liuyulong
 */
public class JwtAuthorizationRenewal implements AuthorizationRenewal<JwtToken, Renewal> {

    private UserManager userManager;

    /**
     * @param userManager
     */
    public JwtAuthorizationRenewal(UserManager userManager) {
        super();
        this.userManager = userManager;
    }

    @Override
    public Renewal renewal(User user, JwtToken token, Consumer<Renewal> consumer) {
        Renewal target = null;
        if (BooleanUtils.isTrue(user.getAllowRenewal())) {
            Renewal renewal = new JwtUtils.Renewal(token, user.getSecretKey(), user.getSecretKeySaltValue()).renewal();
            if (BooleanUtils.isTrue(renewal.getRenewed())) {
                userManager.renewal(user.getAccessKey(), renewal);
                if (null != consumer) {
                    consumer.accept(renewal);
                }
                target = renewal;
            }
        }
        return target;
    }

}
