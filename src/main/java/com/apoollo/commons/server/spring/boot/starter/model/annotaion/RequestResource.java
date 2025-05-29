/**
 * 
 */
package com.apoollo.commons.server.spring.boot.starter.model.annotaion;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import com.apoollo.commons.util.request.context.EscapeMethod;
import com.apoollo.commons.util.request.context.WrapResponseHandler;
import com.apoollo.commons.util.request.context.def.AccessStrategy;
import com.apoollo.commons.util.request.context.def.DefaultEscapeXss;
import com.apoollo.commons.util.request.context.def.DefaultWrapResponseHandler;

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
 * @since 2025年3月20日
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
	 * 请求资源名称，用于日志打印时显示的名称，默认值为resourcePin 属性的值
	 * 
	 * @return 请求资源名称
	 */
	public String name() default "";

	/**
	 * 请求资源路径，用于路径匹配 , controller mapping path + method mapping path
	 * 
	 * @return 请求资源路径
	 */
	public String requestMappingPath() default "";

	/**
	 * 请求资源访问策略，默认私有请求，需要在Header中放入 Authorization 的Jwt Token完成鉴权
	 * 
	 * @return 请求资源访问策略
	 */

	public AccessStrategy accessStrategy() default AccessStrategy.PRIVATE_HEADER_JWT_TOKEN;

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

	/**
	 * 启用 Body 签名，需要客户端将签名放入Header
	 * 
	 * @return 是否开始请求体摘要验证
	 */
	public boolean enableSignature() default false;

	/**
	 * 
	 * @return 客户端请求摘要签名加密的秘钥, 此属性可替换默认值
	 */
	public String signatureSecret() default "";

	/**
	 * 
	 * @return 签名排除的header名称列表
	 */
	public String[] signatureExcludeHeaderNames() default {};

	/**
	 * 
	 * @return 签名包含的header名称列表
	 */
	public String[] signatureIncludeHeaderNames() default {};

	/**
	 * 启用后会过滤Xss字符
	 * 
	 * @return 是否启用请求内容转义
	 */
	public boolean enableContentEscape() default false;

	/**
	 * 要么类有无参构造，要么实例注入到Spring环境中
	 * 
	 * @return 转义方式实现类的Class
	 */

	public Class<? extends EscapeMethod> contentEscapeMethodClass() default DefaultEscapeXss.class;

	/**
	 * 
	 * @return 是否启用返回值包装器
	 */
	public boolean enableResponseWrapper() default true;

	/**
	 * 此字段当启用返回值包装器后生效，自定义异常code与返回值样式，要么类有无参构造，要么实例注入到Spring环境中
	 * 
	 * @return httpCodeNameHandlerClass
	 */
	public Class<? extends WrapResponseHandler> wrapResponseHandlerClass() default DefaultWrapResponseHandler.class;

}
