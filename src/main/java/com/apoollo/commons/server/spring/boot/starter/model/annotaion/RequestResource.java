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

import com.apoollo.commons.util.redis.service.RedisNameSpaceKey.TimeUnitPattern;
import com.apoollo.commons.util.request.context.ContentEscapeMethod;
import com.apoollo.commons.util.request.context.core.AccessStrategy;
import com.apoollo.commons.util.request.context.core.DefaultContentEscapeXss;
import com.apoollo.commons.util.request.context.limiter.NonceValidator;
import com.apoollo.commons.util.request.context.limiter.WrapResponseHandler;
import com.apoollo.commons.util.request.context.limiter.core.DefaultWrapResponseHandler;
import com.apoollo.commons.util.request.context.limiter.core.StrictNonceValidaor;

/**
 * <p>
 * <ol>
 * <li>详细解释详见：https://github.com/apoollo/apoollo-commons-server-spring-boot-starter</li>
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
	public String[] roles() default { "Resource" };

	/**
	 * 
	 * @return 是否开启能力
	 */
	public boolean enableCapacity() default true;

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
	 * 默认为严格的nonce验证器, 需要实例注入到Spring环境中
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
	 * @return 跨域配置, 需要实例注入到Spring环境中
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
	 * @return 允许referer的列表
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
	 * @return 是否启用限制调用次数
	 */
	public boolean enableCountLimiter() default false;

	/**
	 * 
	 * @return 时间单位模式，用于限制时间维度
	 */
	public TimeUnitPattern countLimiterTimeUnitPattern() default TimeUnitPattern.DAY;

	/**
	 * 限制调用次数
	 * 
	 * @return 默认-1
	 */
	public long countLimiterLimitCount() default -1;

	/**
	 * 启用后会过滤Xss字符
	 * 
	 * @return 是否启用请求内容转义
	 */
	public boolean enableContentEscape() default false;

	/**
	 *  需要实例注入到Spring环境中
	 * 
	 * @return 转义方式实现类的Class
	 */

	public Class<? extends ContentEscapeMethod> contentEscapeMethod() default DefaultContentEscapeXss.class;

	/**
	 * 
	 * @return 是否启用返回值包装器
	 */
	public boolean enableResponseWrapper() default false;

	/**
	 * 此字段当启用返回值包装器后生效，自定义异常code与返回值样式, 需要实例注入到Spring环境中
	 * 
	 * @return httpCodeNameHandlerClass
	 */
	public Class<? extends WrapResponseHandler> wrapResponseHandler() default DefaultWrapResponseHandler.class;

}
