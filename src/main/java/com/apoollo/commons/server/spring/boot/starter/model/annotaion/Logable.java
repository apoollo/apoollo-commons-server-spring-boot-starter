/**
 * 
 */
package com.apoollo.commons.server.spring.boot.starter.model.annotaion;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author liuyulong
 * @since 2024-11-23
 */

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.PARAMETER })
@Documented
public @interface Logable {

	public boolean enable() default true;

	public String[] maskProperies() default {};
}
