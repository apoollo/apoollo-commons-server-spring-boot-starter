[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](https://www.gnu.org/licenses/gpl-3.0)

Apoollo Commons Server Spring Boot Starter
====
`apoollo-commons-server` 是一个`SpringBoot Starter`，随 SpringBoot 启动自动加载生效，旨在帮助 Java 开发者快速构建 `Web Server` 服务。应用该 Starter 后，会快速获得管理接口的能力。
比如：`NONCE 限制`、`签名限制`、`跨域限制`、`IP限制`、`REFERER限制`、`同步请求限制`、`流量限制`、`请求数量限制`、`内容转义`、`统一返回值`、`用户身份认证`、`用户授权`、`公有与私有访问`、`统一入参出参日志打印`、`动态静态接口管理`。
单独启动，可以作为一个`独立的网关`。嵌入SpringBoot项目中，可以便捷的将接口转化为一个安全的标准接口，提供统一的标准输出。

工作原理
----

![image](https://github.com/user-attachments/assets/32172c2c-2924-4710-b8d6-82c8269c8760)


##### 将目标函数（Taget MVC Method）变成一个安全接口，要请求目标函数, 需要过一些列的安全检查，每一个阶段都可以动态拔插。同样可以实现框架内特性与非框架特性的混合模式。

#### 1、请求流程拔插

阶段                               |说明 
-----------------------------------|----------------------------------------
PLATFORM_LIMIERS                   |平台级别的限制，可单独设置平台级别的CAPACITY_SUPPORT
REOURCE_LIMIERS                    |资源级别的限制，可单独设置资源级别的CAPACITY_SUPPORT
USER AUTHENTICATION                |用户身份认证，可以选择认证或者不认证，认证则是一个私有访问，不认证则是一个公有访问
USER AUTHORIZATION                 |用户授权认证，可以选择用户对资源的授权范围 
USER_LIMIERS                       |用户级别的限制，可单独设置用户级别的CAPACITY_SUPPORT
TARGET_METHOD_PARAMETER_LOGGING    |目标函数的日志，可选入参与出参打印日志以及参数脱敏打印

#### 2、能力支持拔插（CAPACITY_SUPPORT）


阶段                               |说明 
-----------------------------------|----------------------------------------
NONCE_LIMIER                       |nonce 验证，可以预防重放攻击，一般配合签名限制一起使用
SIGNATURE_LIMITER                  |签名验证，可以预防请求被篡改
CORS_LIMITER                       |跨域验证
IP_LIMITER                         |IP 验证
REFER_LIMITER                      |Referer 验证
SYNC_LIMITER                       |同步请求验证
FLOW_LIMITER                       |Qps 验证
COUNT_LIMTER                       |一段时间内请求数量验证
CONTENT_ESCAPE                     |请求内容转义，可以预防Xss
RESPONSE_WRAPPER                   |响应内容包装成一个标准返回值

#### 3、CAPACITY_SUPPORT 的请求模式

![image](https://github.com/user-attachments/assets/a7c5384b-b2b4-4ee1-90ac-8e533f2c2ead)
##### 每一条线表示一种穿过CAPACITY_SUPPORT的模式，注意：资源级别、用户级别的能力是叠加的。
模式                               |说明                                                                            
-----------------------------------|--------------------------------------------------------------------------------
平台级别                            |所有被框架接收到的请求全都会应用平台级别的限制                                      
资源级别                            |该资源的请求会应用资源本身设置的限制 + 平台级别                                               
用户级别                            |该用户的请求会应用用户本身设置的限制，用户能动态选择适用资源的范围 + 资源级别 + 平台级别                 

Required
----
项目依赖的环境列表
* \>= Jdk17 , 如果需要更换Jdk版本，可以变更`apoollo-dependencies-jdk17`中相关的版本, 具体版本会受SpringBoot版本制约， 默认版本为Jdk17
* \>= SpringBoot3.2.4 ，如果需要更换SpringBoot版本，可以变更`apoollo-dependencies-jdk17`中相关的版本，默认版本为SpringBoot3.2.4
* \>= Maven3.9.3 

Install
----
项目依赖于`apoollo-dependencies-jdk17` 跟 `apoollo-commons-util`, 需要先 clone `apoollo-dependencies-jdk17`、`apoollo-commons-util`、`apoollo-commons-server-spring-boot-starter` 这三个项目，
每个项目根目录都需要先执行 Maven 安装命令

```Bash
git clone https://github.com/apoollo/apoollo-dependencies-jdk17 & \
cd apoollo-dependencies-jdk17 & \
mvn clean install -Dmaven.test.skip=true & \
cd .. & \
git clone https://github.com/apoollo/apoollo-commons-util & \
cd apoollo-commons-util & \
mvn clean install -Dmaven.test.skip=true & \
cd .. & \
git clone https://github.com/apoollo/apoollo-commons-server-spring-boot-starter & \
cd apoollo-commons-server-spring-boot-starter & \
mvn clean install -Dmaven.test.skip=true
```

引入单个Maven POM, 需要指定apoollo-commons-server-spring-boot-starter版本
```Xml
<dependency>
  <groupId>com.apoollo</groupId>
  <artifactId>apoollo-commons-server-spring-boot-starter</artifactId>
  <version>${dependencies-jdk17.version}</version>
</dependency>
```
或者集成式引入Maven POM，需指定apoollo-dependencies-jdk17版本
```Xml
<parent>
  <groupId>com.apoollo</groupId>
  <artifactId>apoollo-dependencies-jdk17</artifactId>
  <version>3.2.4-SNAPSHOT</version>
</parent>
<dependencies>
  <dependency>
    <groupId>com.apoollo</groupId>
    <artifactId>apoollo-commons-server-spring-boot-starter</artifactId>
    <version>${dependencies-jdk17.version}</version>
  </dependency>
</dependencies>
```

依赖Redis环境配置，Redis配置方式以及版本跟SpringBoot版本一致，其中 apoollo 部分的配置为可选配置。所有被path匹配到的路径全会被框架拦截，会应用框架特性，不被拦截的路径不会应用框架特性。适用混合模式的应用场景
```Yaml
spring:
  data:
    redis:
      cluster:
       nodes:
        - 192.168.12.220:6379
        - 192.168.12.220:6380
      password: apoollo@123456
      lettuce:
        pool:
          enabled: true
          max-active: 8
          max-idle: 8
          time-between-eviction-runs: 60000
        cluster:
          refresh:
            adaptive: true
            period: 30000
# apoollo:
#  commons:
#    server:
#      path:
#        include-path-patterns:
#        - /*
```
注意
----
1. 多个项目公用同一个Redis实例或者集群会发生Key冲突，此时应该给每个子项目设置自己的Prefix，具体配置如下
```Java
@Bean
RedisNameSpaceKey getRedisNameSpaceKey() {
	return () -> "apoollo.demo";
}
```

以注解的方式接管资源
----

将 `@RequestResource` 注入到 @RestController 中的函数上，该函数就拥有了一系列的魔法能力
```Java
@RestController
public class DemoController {

	@GetMapping("/demo1")
	@RequestResource(name = "演示1")
	public String demo1() {
		return "I'm OK";
	}
}
```
尝试请求该函数则返回如下JSON

```Bash
curl --location 'http://127.0.0.1:8080/demo1'
```

```JSON
{
    "code": 42100,
    "data": null,
    "success": false,
    "requestId": "E0FF78F752BE4F34A570BA2950E5F15B",
    "name": "AuthenticationJwtTokenIllegal",
    "message": "authorizationJwtToken must not be blank",
    "elapsedTime": 1
}
```
code 属性值为 `42100`，name 属性值为 `AuthenticationJwtTokenIllegal` ，这说明该函数默认被 `@RequestResource` 注解后，`默认置为私有访问了，得通过授权的token才能访问，同时将返回值变成一个JSON`


以登录方式获取Token
----

浏览器窗口，用户登录后的情况下获取Token，执行以下函数，表示用户登录成功后向缓存设置登录信息，并返回登录token
```Java
@Autowired
private com.apoollo.commons.server.spring.boot.starter.service.UserManager userManager;

//授权匹配条件
UserMatchesRequestResourceCondition authenticationCondition = new UserMatchesRequestResourceCondition();
//包含角色为Resource的资源都会被该用户访问到
authenticationCondition.setIncludeRoles(List.of("Resource"));

SerializableUser user = new SerializableUser();
user.setId("id");// 用户id
user.setEnable(true);// 表示用户状态有效
user.setAccessKey("accessKey");// 用户身份标识
user.setSecretKey("secretKey");// 用户秘钥
user.setAuthorizationCondition(authenticationCondition);// 设置授权匹配条件

user.setSecretKeySsoSalt(LangUtils.getUppercaseUUID());//每次登录设置一个随机值，会支持单点登录

// 同时设置以下两个字段不为null，response header "x-user-password-expired" 字段会标记 true 或者 false 提示用户修改密码
user.setPasswordLastUpdateTimestamp(System.currentTimeMillis());// 设置上次密码修改的时间戳，
user.setPasswordValidMillis(1000L * 60 * 60 * 24 * 30);// 设置密码有效毫秒时长

Stirng token = userManager.login(//
	user,//身份信息
	30L, // token 过期时长
	TimeUnit.MINUTES //token 过期时长的单位时间
	);
```

以服务之间相互调用的方式获取Token
----
服务端需要提前设置有效用户
```Java
@Autowired
private com.apoollo.commons.server.spring.boot.starter.service.UserManager userManager;
//按照User属性设置
SerializableUser user = new SerializableUser();
userManager.setUser(
         user, // 注册
	null,// 服务端设置用户一般不设置超时时间
	null // 服务端设置用户一般不设置超时时间
);
```

客户端需要通过有效用户的accessKey、secretKey生成Token请求目标接口
```Java
String token = com.apoollo.commons.util.JwtUtils.generateJwtToken(
	 "accesskey", //用户名
 	 "secretKey,// 密码
	 null, // 用于单点登录，设置null值
	 new Date(),// 颁发时间，当前时间
	 new Date(System.currentTimeMillis() + 10000) // 过期截止时间，建议设置10s左右
);
```

带token的方式请求私有资源
----
带token请求该函数则返回如下JSON
```Bash
curl --location 'http://127.0.0.1:8080/demo1' \
--header 'Authorization: ${token}'
```
```JSON
{
    "code": 20000,
    "data": "I'm OK",
    "success": true,
    "requestId": "B0939A3EE9524BE085BFB4D1D1049610",
    "name": "Ok",
    "message": "success",
    "elapsedTime": 129
}
```
code 为 20000 ，name 为 Ok ，表示后端验证通过，请求成功，并且data字段返回了函数的返回值I'm OK，这样就完成一次请求私有函数的验证

日志
----
单次请求的业务日志，包含了 MDC 标记、请求标记、访问URI、请求IP、入参内容、出参内容、请求耗时、结束标记
```Java
2025-06-26T10:23:27.555+08:00  INFO --- [http-nio-8080-exec-3] com.apoollo.commons.server.spring.boot.starter.component.filter.RequestContextFilter.doPreSecureFilter(RequestContextFilter.java:70) :  B0939A3EE9524BE085BFB4D1D1049610 请求进入标记
2025-06-26T10:23:27.555+08:00  INFO --- [http-nio-8080-exec-3] com.apoollo.commons.server.spring.boot.starter.component.filter.RequestContextFilter.doPreSecureFilter(RequestContextFilter.java:83) :  B0939A3EE9524BE085BFB4D1D1049610 访问URI：/demo1
2025-06-26T10:23:27.555+08:00  INFO --- [http-nio-8080-exec-3] com.apoollo.commons.server.spring.boot.starter.component.filter.RequestContextFilter.doPreSecureFilter(RequestContextFilter.java:84) :  B0939A3EE9524BE085BFB4D1D1049610 访问IP：127.0.0.1
2025-06-26T10:23:27.555+08:00  INFO --- [http-nio-8080-exec-3] com.apoollo.commons.util.request.context.access.core.DefaultRequestResourceManager.getRequestResource(DefaultRequestResourceManager.java:59) :  B0939A3EE9524BE085BFB4D1D1049610 getRequestResource elapsedTime：0ms
2025-06-26T10:23:27.557+08:00  INFO --- [http-nio-8080-exec-3] com.apoollo.commons.util.request.context.access.core.DefaultUserManager.getUser(DefaultUserManager.java:83) :  B0939A3EE9524BE085BFB4D1D1049610 getUser elapsedTime：1ms
2025-06-26T10:23:27.557+08:00  INFO --- [http-nio-8080-exec-3] com.apoollo.commons.util.request.context.access.core.DefaultAuthorization.authorize(DefaultAuthorization.java:76) :  B0939A3EE9524BE085BFB4D1D1049610 authorized elapsedTime：0ms
2025-06-26T10:23:27.558+08:00  INFO --- [http-nio-8080-exec-3] com.apoollo.commons.server.spring.boot.starter.component.aspect.RequestResourceAspect.print(RequestResourceAspect.java:99) :  B0939A3EE9524BE085BFB4D1D1049610 "演示1" - 出参: I'm OK
2025-06-26T10:23:27.558+08:00  INFO --- [http-nio-8080-exec-3] com.apoollo.commons.server.spring.boot.starter.component.aspect.RequestResourceAspect.advice(RequestResourceAspect.java:158) :  B0939A3EE9524BE085BFB4D1D1049610 "演示1" - 耗时: 0(ms)
2025-06-26T10:23:27.559+08:00  INFO --- [http-nio-8080-exec-3] com.apoollo.commons.server.spring.boot.starter.component.filter.RequestContextFilter.cleanupMatches(RequestContextFilter.java:112) :  B0939A3EE9524BE085BFB4D1D1049610 total elapsedTime：3ms
2025-06-26T10:23:27.559+08:00  INFO --- [http-nio-8080-exec-3] com.apoollo.commons.server.spring.boot.starter.component.filter.RequestContextFilter.cleanupMatches(RequestContextFilter.java:120) :  B0939A3EE9524BE085BFB4D1D1049610 请求结束标记
```

正常情况下，被@RequestResource注解的请求资源会自动打印出入参日志。特殊情况下可能不想打印，或者特殊属性需要脱敏显示。这个情况可以由@Logable 调整。全路径：com.apoollo.commons.server.spring.boot.starter.model.annotaion.Logable，设置在函数上是针对返回值的内容，设置在参数上针对参数
属性               |默认值                                                                                            |说明
-------------------|--------------------------------------------------------------------------------------------------|---------------------------------------------------------
enable             |true 启用                                                                                         |false 会关闭日志打印
maskProperies      | {}  空对象，显示所有字段                                                                           |设置字段后，会对字段做 *** 打印的方式。仅对对象属性有效

@RequestResource 注解
----
全路径：com.apoollo.commons.server.spring.boot.starter.model.annotaion.RequestResource，此注解表示一个静态资源实体，仅对Controller 中的@RequestMapping、@GetMapping、@PostMapping、@PutMapping、@DeleteMapping生效
注入该注解会接管接口请求的前置条件以及后置条件，这里的字段属性并不完整，其他关于限制能力的部分，可以参见：CAPACITY_SUPPORT 属性

属性                         |默认值                                                       |说明
-----------------------------|-------------------------------------------------------------|---------------------------------------------------------
enable                       |true                                                         |是否启用注解特性
resourcePin                  |Controller名称(首字母小写) +  Method 名换                      |唯一标识符
name                         |resourcePin 属性的值                                          |名称，用于日志打印时显示
requestMappingPath           |跟@RequestMapping注解的值一致                                  |请求资源路径，用于路径匹配，RequestMapping不建议使用多个URL
accessStrategy               |PRIVATE_HEADER_JWT_TOKEN 私有访问                             |详细见：访问策略 AccessStrategy
roles                        |Resource 默认资源角色为资源角色                                |如果用户的角色与资源的角色匹配能够匹配，也会完成私有访问的权限验证，访问可以通过
enableCapacity               |true 表示默认开启资源级别的限制能力                             |具体限制有单独开关，此属性为true是其他限制能力的前提条件


#### 访问策略 AccessStrategy


属性                         |说明
-----------------------------|---------------------------------------------------------
PUBLIC                       |公有访问，无需鉴权Token，可直接访问
PRIVATE_HEADER_JWT_TOKEN     |私有访问，需要在Header中放入 Authorization 的Jwt Token完成鉴权
PRIVATE_HEADER_KEY_PAIR      |私有访问，需要在Header中放入秘钥对，header密钥对的key可以通过配置文件配置，默认值为：accessKey、secretKey
PRIVATE_PARAMETER_KEY_PAIR   |私有访问，需要在parameter中获取秘钥对，跟PRIVATE_HEADER_KEY_PAIR 区别是获取密钥对的位置不一致
PRIVATE_JSON_BODY_JWT_TOKEN  |私有访问，需要在Body 的JSON根节点中放入 Authorization 属性，值为Jwt Token，与PRIVATE_HEADER_JWT_TOKEN的区别是获取Token的位置不一致
PRIVATE_JSON_BODY_KEY_PAIR   |私有访问，需要在Body 的JSON根节点中放入密钥对属性，与PRIVATE_HEADER_KEY_PAIR的区别是获取密钥对的位置不一致

CAPACITY_SUPPORT 实例
----

##### 平台实例、资源实例、用户实例，通过公共属性可控制每种资源的限制粒度，注意实例之间的向下叠加性

#### 1、自定义平台实例


```Java
@Bean
SerializebleCapacitySupport getSerializebleCapacitySupport() {
	SerializebleCapacitySupport capacitySupport = new SerializebleCapacitySupport();
	capacitySupport.setEnableCapacity(true);
	capacitySupport.setEnableResponseWrapper(true);
	capacitySupport.setResourcePin("platform");
	capacitySupport.setWrapResponseHandlerClass(WrapResponseHandler.class);
        capacitySupport.setEnableContentEscape(true);
	return capacitySupport;
}
```
#### 2、自定义资源实例


1. 使用注解的方式

```Java
@GetMapping("/demo2")
@RequestResource(name = "演示2", accessStrategy = AccessStrategy.PRIVATE_JSON_BODY_JWT_TOKEN,  enableContentEscape = false, enableNonceLimiter = false)
public String demo1() {
	return "I'm OK";
}
```
2. 使用注册的方式

```Java
@Autowired
private com.apoollo.commons.server.spring.boot.starter.service.RequestResourceManager requestResourceManager;

SerializableRequestResource requestResource = new SerializableRequestResource();
requestResource.setEnableCapacity(true);
requestResource.setEnableResponseWrapper(true);
requestResource.setWrapResponseHandlerClass(WrapResponseHandler.class);
requestResource.setEnableContentEscape(true);
//......其他属性设置
requestResourceManager.setRequestResource(requestResource); 
```
#### 3、自定义用户实例


```Java
@Autowired
private com.apoollo.commons.server.spring.boot.starter.service.UserManager userManager;

//按照User属性设置
SerializableUser user = new SerializableUser();
user.setEnableCapacity(true);
user.setEnableResponseWrapper(true);
user.setWrapResponseHandlerClass(WrapResponseHandler.class);
user.setEnableContentEscape(true);
//......其他属性设置
userManager.setUser(user, 30L, TimeUnit.MINUTES);
```
#### 4、CAPACITY_SUPPORT 属性


属性                                              |说明
--------------------------------------------------|---------------------------------------------------------
enableCapacity                                    |CAPACITY 的总开关，此字段设置为false，将会禁止所有的CAPACITY
enableNonceLimiter                                |启用后，会验证 header 中的x-nonce、x-timestamp
nonceLimiterDuration                              |nonce的有效时长
nonceLimiterValidator                             |nonce的验证器实现类的Class, 自定义需要将实例注入到Spring环境中
enableSignatureLimiter                            |是否启用签名限制，启用后，会验证 header 中的x-signature
signatureLimiterSecret                            |请求摘要加密的秘钥, 摘要加密后就是签名
signatureLimiterExcludeHeaderNames                |签名排除的header名称列表
signatureLimiterIncludeHeaderNames                |签名包含的header名称列表
enableCorsLimiter                                 |是否启用跨域配置
corsLimiterConfiguration                          |跨域配置的Class，自定义需要将实例注入到Spring环境中
enableIpLimiter                                   |是否启用IP限制
ipLimiterExcludes                                 |IP 黑名单
ipLimiterIncludes                                 |IP 白名单
enableRefererLimiter                              |是否启用限制referer
refererLimiterIncludeReferers                     |允许referer的列表
enableSyncLimiter                                 |是否开启同步模式，启用后，该请求资源只允许序列请求，不允许并发请求
enableFlowLimiter                                 |是否启用每秒限流
flowLimiterLimitCount                             |每秒限流数量
enableCountLimiter                                |是否启用限制调用次数
countLimiterTimeUnitPattern                       |限制次数时间单位模式，用于限制时间维度
countLimiterLimitCount                            |限制调用次数的数量
enableContentEscape                               |是否启用请求内容转义
contentEscapeMethod                               |转义方式实现类的Class，自定义需要将实例注入到Spring环境中
enableResponseWrapper                             |是否启用返回值包装器
wrapResponseHandler                               |包返回值实现类的Class，自定义需要实例注入到Spring环境中，此字段当启用返回值包装器后生效，自定义异常code与返回值样式

包装返回值
----
包装返回值中预定义了code、name、httpCode、success 四者之间的关系，默认实现为com.apoollo.commons.util.request.context.limiter.core.DefaultWrapResponseHandler，可以为每个资源或者用户自定义自己的实现，完成不同资源、不同用户可以有不同的返回值。
1. 41000-41999 之间是SpringBoot级别预定义编码，是请求报文编排异常，需要与服务器核对接口定义
2. 42000-42999 之间是本框架预定义编码，是请求报文编排异常，需要与服务器核对接口定义
3. 43000-49999 之间是给应用预留的业务编码，应用抛出异常可以从这些CODE里自定义编辑，通过com.apoollo.commons.util.exception.AppHttpCodeNameMessageException异常抛出，可以被系统捕获
4. 50000-59999 之间是系统异常，属于系统BUG

code       |name                                               |success          |httpCode       |说明 
-----------|---------------------------------------------------|-----------------|---------------|----------------
20000      |Ok                                                 |true             |200            |请求成功
41000      |HttpRequestMethodNotSupported                      |false            |200            |请求方式不支持
41001      |HttpMediaTypeNotSupported                          |false            |200            |请求媒体类型不支持
41002      |HttpMediaTypeNotAcceptable                         |false            |200            |请求媒体类型不被接受
41003      |MissingPathVariable                                |false            |200            |丢失路径变量
41004      |MissingServletRequestParameter                     |false            |200            |请求参数丢失
41005      |MissingServletRequestPart                          |false            |200            |请求文件参数丢失
41006      |ServletRequestBinding                              |false            |200            |请求绑定异常
41007      |MethodArgumentNotValid                             |false            |200            |请求参数不合法
41008      |HandlerMethodValidation                            |false            |200            |参数验证失败
41009      |NoHandlerFound                                     |false            |200            |没有找到处理器
41010      |NoResourceFound                                    |false            |200            |没有找到资源
41011      |AsyncRequestTimeout                                |false            |200            |异步请求超时
41012      |TypeMismatch                                       |false            |200            |类型不匹配
41013      |HttpMessageNotReadable                             |false            |200            |请求消息不可读
41014      |BindError                                          |false            |200            |绑定异常
42000      |ClientRequestIdIllegal                             |false            |200            |客户端请求id非法
42001      |RequestResourceNotExists                           |false            |200            |资源不存在
42002      |ResourceDisabled                                   |false            |200            |资源被禁用
42010      |NonceLimiterRefused                                |false            |200            |Nonce验证拒绝
42011      |NonceLimiterTimestampIllegal                       |false            |200            |Nonce时间戳非法
42020      |SignatureLimiterSignatureRefused                   |false            |200            |签名限制拒绝
42030      |CorsLimiterRefused                                 |false            |200            |跨域限制拒绝
42040      |IpLimiterExcludeListRefused                        |false            |200            |IP黑名单限制拒绝
42041      |IpLimiterIncludeListRefused                        |false            |200            |IP白名单限制拒绝
42050      |RefererLimiterRefused                              |false            |200            |Referer限制拒绝
42060      |SyncLimiterRefused                                 |false            |200            |同步限制拒绝
42070      |FlowLimiterRefused                                 |false            |200            |流量限制拒绝
42080      |CountLimiterRefused                                |false            |200            |数量限制拒绝
42090      |AuthenticationAccessKeyIllegal                     |false            |200            |身份认证身份标识非法
42091      |AuthenticationTokenIllegal                         |false            |200            |身份认证Token非法
42092      |AuthenticationUserDisabled                         |false            |200            |身份认证用户被禁止
42100      |AuthenticationJwtTokenIllegal                      |false            |200            |身份认证JWT Token非法
42101      |AuthenticationJwtTokenExpired                      |false            |200            |身份认证JWT Token过期
42102      |AuthenticationJwtTokenForbidden                    |false            |200            |身份认证JWT Token验证未通过
42110      |AuthenticationKeyPairTokenIllegal                  |false            |200            |身份认证密钥对非法
42111      |AuthenticationKeyPairSecretKeyForbidden            |false            |200            |身份认证秘钥验证未通过
42120      |AuthorizationForbidden                             |false            |200            |请求资源未被授权
42130      |ServerOverloaded                                   |false            |200            |服务器负载过高
42998      |ParameterIllegal                                   |false            |200            |参数不合法
42999      |BadRequest                                         |false            |200            |错误请求
50000      |SystemError                                        |false            |200            |系统异常
50001      |ConversionNotSupported                             |false            |200            |转化器不支持
50002      |HttpMessageNotWritable                             |false            |200            |消息不能写入
50003      |MethodValidation                                   |false            |200            |方法验证异常
50004      |AsyncRequestNotUsable                              |false            |200            |异步请求不可用




