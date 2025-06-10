/**
 * 
 */
package com.apoollo.commons.server.spring.boot.starter.model.annotaion;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.springframework.web.cors.CorsConfiguration;

import com.apoollo.commons.util.request.context.EscapeMethod;
import com.apoollo.commons.util.request.context.core.AccessStrategy;
import com.apoollo.commons.util.request.context.core.DefaultEscapeXss;
import com.apoollo.commons.util.request.context.limiter.NonceValidator;
import com.apoollo.commons.util.request.context.limiter.WrapResponseHandler;
import com.apoollo.commons.util.request.context.limiter.core.DefaultWrapResponseHandler;
import com.apoollo.commons.util.request.context.limiter.core.StrictNonceValidaor;

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
	 * 请求资源路径，用于路径匹配 , method mapping path
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
	 * 如果用户的角色与资源的角色匹配，才会有权限访问，默认资源角色为用户角色
	 * 
	 * @return 资源所属的角色列表
	 */
	public String[] roles() default { "User" };

	/**
	 * 
	 * @return 是否开启能力
	 */
	public boolean enableCapacity() default false;

	/**
	 * 启用后，会验证 header 中的x-nonce、x-timestamp
	 * 
	 * @return 是否启用nonce 验证
	 */
	public boolean enableNonceLimiter() default false;

	/**
	 * 默认10000毫秒
	 * 
	 * @return nonce的有效时长
	 */
	public long nonceLimiterDuration() default 10000;

	/**
	 * 默认为严格的nonce验证器
	 * 
	 * @return nonce 验证器
	 */
	public Class<? extends NonceValidator> nonceLimiterValidator() default StrictNonceValidaor.class;

	/**
	 * 启用后，会验证 header 中的x-signature
	 * 
	 * @return 是否请求签名验证
	 */
	public boolean enableSignatureLimiter() default false;

	/**
	 * 
	 * @return 请求摘要加密的秘钥, 摘要加密后就是签名， 此属性可替换默认值
	 */
	public String signatureLimiterSecret() default "";

	/**
	 * 
	 * @return 签名排除的header名称列表
	 */
	public String[] signatureLimiterExcludeHeaderNames() default {};

	/**
	 * 
	 * @return 签名包含的header名称列表
	 */
	public String[] signatureLimiterIncludeHeaderNames() default {};

	/**
	 * 
	 * @return 是否启用跨域配置
	 */
	public boolean enableCorsLimiter() default false;

	/**
	 * 
	 * @return 跨域配置
	 */
	public Class<? extends CorsConfiguration> corsLimiterConfiguration() default CorsConfiguration.class;

	/**
	 * 
	 * @return 是否启用IP限制
	 */
	public boolean enableIpLimiter() default false;

	/**
	 * 
	 * @return IP 黑名单
	 */
	public String[] ipLimiterExcludes() default {};

	/**
	 * 
	 * @return IP 白名单
	 */
	public String[] ipLimiterIncludes() default {};

	/**
	 * 
	 * @return 是否启用限制referer
	 */
	public boolean enableRefererLimiter() default false;

	/**
	 * 
	 * @return 限制Referer列表
	 */
	public String[] refererLimiterIncludeReferers() default {};

	/**
	 * 设置true 该请求资源只允许序列请求，不允许并发请求
	 * 
	 * @return 是否开启同步模式
	 */
	public boolean enableSyncLimiter() default false;

	/**
	 * 
	 * @return 是否启用每秒限流
	 */
	public boolean enableFlowLimiter() default false;

	/**
	 * 
	 * @return 每秒限流数量
	 */
	public long flowLimiterLimitCount() default -1;

	/**
	 * 
	 * @return 是否启用限制每日调用次数
	 */
	public boolean enableDailyCountLimiter() default false;

	/**
	 * 限制每日调用次数
	 * 
	 * @return 默认-1
	 */
	public long dailyCountLimiterLimitCount() default -1;

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
	public boolean enableResponseWrapper() default false;

	/**
	 * 此字段当启用返回值包装器后生效，自定义异常code与返回值样式，要么类有无参构造，要么实例注入到Spring环境中
	 * 
	 * @return httpCodeNameHandlerClass
	 */
	public Class<? extends WrapResponseHandler> wrapResponseHandler() default DefaultWrapResponseHandler.class;

}
