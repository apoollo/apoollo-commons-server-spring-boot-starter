/**
 * 
 */
package com.apoollo.commons.server.spring.boot.starter.service;

import java.util.function.Consumer;

import com.apoollo.commons.util.request.context.User;

/**
 * @author liuyulong
 */
public interface AuthorizationRenewal<T, R> {

    public R renewal(User user, T token, Consumer<R> consumer);

    public default R renewal(User user, T token) {
        return renewal(user, token, null);
    }
}
