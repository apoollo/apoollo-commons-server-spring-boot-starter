/**
 * 
 */
package com.apoollo.commons.server.spring.boot.starter.model.annotaion;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import com.apoollo.commons.util.request.context.RequestResourceAccessStrategy;
import com.apoollo.commons.util.request.context.def.AccessStrategy;
import com.apoollo.commons.util.request.context.def.PrivateRequestResourceAccessStrategy;
import com.apoollo.commons.util.request.context.def.ResourceType;

/**
 * <p>
 * 使用此注解可获得的能力
 * <ol>
 * <li>用户维度限流：默认关闭</li>
 * <li>平台维度限流：默认关闭</li>
 * <li>同步执行：默认关闭</li>
 * <li>统一返回值：默认统一返回样式</li>
 * <li>统一异常：默认异常体系</li>
 * <li>统一权限管理：默认私有访问需要授权，可以通过URL匹配授权，也可以通过角色授权</li>
 * <li>全局MDC以及入参出参日志管理：默认开启打印入参出参日志，可单独关闭。配合 {@code @Logable }
 * 使用，可对指定字段脱敏打印</li>
 * <li>会得到 {@code RequestContext.getRequired();}</li>
 * <li>支持通过 {@code RequestResourceManager} 支持动态注入资源</li>
 * <li>支持通过 {@code UserManager} 动态授权</li>
 * </ol>
 * </p>
 * 
 * @author liuyulong
 * @since 2023年11月20日
 * @see com.apoollo.commons.server.spring.boot.starter.model.annotaion.Logable
 * 
 */
@Documented
@Retention(RUNTIME)
@Target(METHOD)
public @interface RequestResource {

	/**
	 * 如果等于false，则不生效
	 * 
	 * @return 是否可用
	 */
	public boolean enable() default true;

	/**
	 * 请求资源唯一标识符，用于组织与其他实体的关系
	 * 
	 * @return 请求资源唯一标识符
	 */

	public String resourcePin() default "";

	/**
	 * 请求资源名称，用于日志打印时显示的名称
	 * 
	 * @return 请求资源名称
	 */
	public String name() default "";

	/**
	 * 请求资源路径，用于路径匹配
	 * 
	 * @return 请求资源路径
	 */
	public String requestMappingPath() default "";

	/**
	 * 请求资源访问策略，默认私有请求，需要在Header中放入 Authorization 的Jwt Token完成鉴权
	 * 
	 * @return 请求资源访问策略
	 */

	public AccessStrategy accessStrategy() default AccessStrategy.PRIVATE_REQUEST;

	/**
	 * 自定义访问策略的Class
	 * 
	 * @return Class<RequestResourceAccessStrategy>
	 */
	public Class<? extends RequestResourceAccessStrategy> customizeAccessStrategyClass() default PrivateRequestResourceAccessStrategy.class;

	/**
	 * 请求资源用户维度QPS，默认不限制
	 * 
	 * @return 请求资源用户维度QPS
	 */
	public long limtUserQps() default -1;

	/**
	 * 请求资源平台维度QPS，默认关闭平台限流
	 * 
	 * @return 请求资源平台维度QPS
	 */
	public long limtPlatformQps() default -1;

	/**
	 * 请求资源类型，默认为静态类型
	 * 
	 * @return 请求资源类型
	 */
	public ResourceType resourceType() default ResourceType.STATIC;

	/**
	 * 如果用户的角色与资源的角色匹配，才会有权限访问，默认资源角色为用户角色
	 * 
	 * @return 资源所属的角色列表
	 */
	public String[] roles() default { "User" };

	/**
	 * 设置true 该请求资源只允许序列请求，不允许并发请求
	 * 
	 * @return 是否开启同步模式
	 */
	public boolean enableSync() default false;

}
