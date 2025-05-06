[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](https://www.gnu.org/licenses/gpl-3.0)

Apoollo Commons Server Spring Boot Starter
====
`apoollo-commons-server` 是一个`SpringBoot Starter`，随 SpringBoot 启动自动加载生效，旨在帮助 Java 开发快速构建 `Web Server` 服务。应用该 Starter 后，会快速获得管理接口的能力。
比如： `用户维度的限流`、`平台维度的限流`、`请求同步`、`统一返回值`、`接口公有私有访问`、`统一入参出参日志打印`、`动态静态接口管理`。
单独启动，可以作为一个`独立的网关`。嵌入SpringBoot项目中，可以节省一些开发时间，让开发人员专注于业务逻辑实现。

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
mvn clean install -Dmaven.test.skip=true & \
```

引入Maven POM
```Xml
<dependency>
  <groupId>com.apoollo</groupId>
  <artifactId>apoollo-commons-server-spring-boot-starter</artifactId>
  <version>${dependencies-jdk17.version}</version>
</dependency>
```
依赖Redis环境配置，Redis配置方式以及版本跟SpringBoot版本一致
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
```

以注解注入资源
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
    "requestId": "0F0BD9D421764E4D9EBC0336575C8EF7",
    "success": false,
    "code": "Forbidden",
    "message": "访问无权限:authorizationJwtToken must not be blank",
    "elapsedTime": 7,
    "data": null
}
```
code 属性为 `Forbidden`，这说明该函数默认被 `@RequestResource` 注解后，`默认置为私有访问了，得通过授权的token才能访问，同时将返回值变成一个JSON`


用户登录的方式获取Token
----

Web前端，用户登录后的情况获取Token，执行以下函数，表示用户登录成功后向缓存设置登录信息，并返回登录token
```Java
@Autowired
private com.apoollo.commons.server.spring.boot.starter.service.UserManager userManager;

Stirng token = userManager.login(//
		"id", // 用户id
		"accessKey", //用户名称
		"secretKey", //用户密码
		"secretKeySaltValue", //密码盐值，设置后可以实现单点登录，通常设置为一个随机数或者UUID
		true, //是否支持续期，设置为true后，response header 中会在过期时长超过3/2的时候返回 x-renewal-authorization 字段，来替换旧的Token，前端可以替换使用
		null, // Ip 白名单列表，配置apoollo.commons.server.access.limit-ip.enable=true 后才生效
		List.of("/demo1"), // 该用户被允许请求的列表，支持AntPathMatcher表达式 /**,/abc/* 等
		null, // 该用户的角色列表，如果用户角色跟资源角色匹配的话，也可以允许被访问，资源角色指的是 @RequestResource roles 属性
		null, // 用户其他属性附件，可用于业务处理； 通过 RequestContext.getRequired(); 来获取上下文信息
		null, // 提醒用户更换密码的最后时间
		30L, // token 过期时长
		TimeUnit.MINUTES //token 过期时长的单位时间
	);
```
带token请求该函数则返回如下JSON
```Bash
curl --location 'http://127.0.0.1:8080/demo1' \
--header 'Authorization: ${token}'
```
```JSON
{
    "requestId": "1446E45F531B4493BB54EEEEA3467024",
    "success": true,
    "code": "Ok",
    "message": "通过",
    "elapsedTime": 197,
    "data": "I'm OK"
}
```
code 为 OK ，表示后端验证通过，请求成功，并且data字段返回了函数的返回值I'm OK，这样就完成一次请求私有函数的验证

客户端请求的方式获取Token
----

服务端设置用户
```Java
@Autowired
private com.apoollo.commons.server.spring.boot.starter.service.UserManager userManager;

userManager.setUser(
        new com.apoollo.commons.util.request.context.def.DefaultUser(...), 按照User参数设置
	null,// 服务端设置用户一般不设置超时时间
	null // 服务端设置用户一般不设置超时时间
);
```

客户端生成Token
```Java
String token = com.apoollo.commons.util.JwtUtils.generateJwtToken(
	 "accesskey", //用户名
 	 "secretKey,// 密码
	 null, // 用于单点登录，设置null值
	 new Date(),// 颁发时间，当前时间
	 new Date(System.currentTimeMillis() + 10000) // 过期截止时间，一般设置10s以后
);
```
带token请求该函数则返回如下JSON
```Bash
curl --location 'http://127.0.0.1:8080/demo1' \
--header 'Authorization: ${token}'
```
```JSON
{
    "requestId": "1446E45F531B4493BB54EEEEA3467024",
    "success": true,
    "code": "Ok",
    "message": "通过",
    "elapsedTime": 197,
    "data": "I'm OK"
}
```
code 为 OK ，表示后端验证通过，请求成功，并且data字段返回了函数的返回值I'm OK，这样就完成一次请求私有函数的验证

@RequestResource 属性
----
enable： 是否启用注解特性，默认true
resourcePin: 唯一标识符，默认为Controller名称 + Method 名换，首字母小写
name: 名称，用于日志打印时显示



