[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](https://www.gnu.org/licenses/gpl-3.0)

Apoollo Commons Server Spring Boot Starter
====
`apoollo-commons-server` 是一个`SpringBoot Starter`，随 SpringBoot 启动自动加载生效，旨在帮助 Java 开发快速构建 `Web Server` 服务。应用该 Starter 后，会快速获得管理接口的能力。
比如： `用户维度的限流`、`平台维度的限流`、`请求同步`、`统一返回值`、`接口公有私有访问`、`统一入参出参日志打印`、`动态静态接口管理`。
单独启动，可以作为一个`独立的网关`。嵌入SpringBoot项目中，可以节省一些开发时间，让开发人员专注于业务逻辑实现。

Required
----
项目依赖的环境列表
* \>= Jdk8 , 如果需要更换Jdk版本，可以变更`apoollo-dependencies-jdk17`中相关的版本, 具体版本会受SpringBoot版本制约， 默认版本为Jdk17
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

使用
----

将 `@RequestResource` 注入到 @RestController 中的函数上，该函数就拥有了一系列的魔法能力
```Java
@RestController
public class DemoController {

	@GetMapping("/demo1")
	@RequestResource(name = "演示1")
	public String demo1() {
		return "OK";
	}
}
```
尝试请求该函数则返回一个JSON, code 属性为 `Forbidden`

```JSON
{
    "requestId": "A94816603126426A9DC7F126B88F3569",
    "success": false,
    "code": "Forbidden",
    "message": "访问无权限:signature expired",
    "elapsedTime": 151,
    "data": null
}
```
