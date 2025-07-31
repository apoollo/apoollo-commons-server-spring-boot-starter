create table apoollo_organization
(
id bigint primary key comment '主键id',
pin varchar(32) unique key not null comment '组织唯一标识',
name varchar(50) unique key not null comment '组织名称',
parent_id bigint comment '父组织Id',
root_id bigint comment '根组织id'
)ENGINE=InnoDB COMMENT='组织';

create table apoollo_user
(
id bigint primary key comment '主键id',
pin varchar(32) unique key not null comment '用户唯一标识',
name varchar(50) unique key not null comment '用户名称',
enabled tinyint not null comment '是否启用',
organization_id bigint not null comment '组织id',
password varchar(128) not null comment '用户密码',
password_valid_millis bigint not null comment '密码有效毫秒数',
password_last_update_timestamp bignit  not null comment '密码最后更新时间戳',
enable_force_change_password tinyint not null comment '是否强制变更密码'
)ENGINE=InnoDB COMMENT='用户';


create table apoollo_key
(
id bigint primary key comment '主键id',
user_id bigint not null comment '所属用户id',
access_key char(32) unique key not null comment 'key唯一标识',
secret_key char(32) unique key not null comment 'key秘钥',
enabled tinyint not null comment '是否启用'
)ENGINE=InnoDB COMMENT='用户';

create table apoollo_role
(
id bigint primary key comment '主键id',
role_pin varchar(32) unique key not null comment '角色唯一标识',
role_name varchar(50) unique key not null comment '角色名称'
)ENGINE=InnoDB COMMENT='角色';


create table apoollo_role_user_mapping
(
id bigint primary key comment '主键id',
role_id bigint not null comment '角色id',
user_id bigint not null comment '用户id'
)ENGINE=InnoDB COMMENT='角色用户映射';

create table apoollo_role_organization_mapping
(
id bigint primary key comment '主键id',
role_id bigint not null comment '角色id',
organization_id bigint not null comment '组织id'
)ENGINE=InnoDB COMMENT='角色组织映射';

create table apoollo_menu
(
id bigint primary key comment '主键id',
pin varchar(32) unique key not null comment '菜单唯一标识',
name varchar(50) unique key not null comment '菜单名称',
order_number int not null comment '菜单顺序',
node_type varchar(20) comment '菜单节点类型',
route_url varchar(100) comment '菜单路由地址',
icon varchar(100) commnet '菜单图标',
css_class varchar(100) comment '菜单css类',
parent_id bigint comment '父菜单Id',
root_id bigint comment '根菜单id'
)ENGINE=InnoDB COMMENT='菜单';

create table apoollo_role_menu_mapping
(
id bigint primary key comment '主键id',
role_id bigint not null comment '角色id',
menu_id bigint not null comment '菜单id'
)ENGINE=InnoDB COMMENT='角色菜单映射';


create table apoollo_request_resource
(
id bigint primary key comment '主键id',
pin varchar(32) unique key not null comment '唯一标识',
name varchar(50) unique key not null comment '名称',
enabled tinyint not null comment '是否启用',
request_mapping_path varchar(200) not null comment '请求映射路径',
access_strategy varchar(50) not null comment '请求策略' 
)ENGINE=InnoDB COMMENT='请求资源表';

create table apoollo_role_request_resource_mapping
(
id bigint primary key comment '主键id',
role_id bigint not null comment '角色id',
request_resource_id bigint not null comment '请求资源id'
)ENGINE=InnoDB COMMENT='角色请求资源映射';

create table apoollo_capcity_support
(
id bigint primary key comment '主键id',
effect_on_type varchar(10) not null comment '生效的主体的类型',
effect_on_id bigint not null comment '生效的主体的id',
enabled tinyint not null comment '是否启',
enable_nonce_limiter tinyint not null comment '是否启用nonce限制',
nonce_limiter_duration bigint comment 'nonce限制时长',
enable_signature_limiter tinyint not null comment '是否启用签名限制',
signature_limiter_secret varchar(256) comment '签名秘钥',
signature_limiter_exclude_header_names varchar(500) comment '签名排除的header列表',
signature_limiter_include_header_names varchar(500)comment '签名包含的header列表',
enable_cors_limiter tinyint not null comment '是否启用跨域',
enable_ip_Limiter tinyint not null comment '是否启用ip限制',
enable_ip_Limiter_excludes varchar(1100) comment 'ip限制排除的ip列表',
ip_limiter_includes varchar(1100) comment 'ip限制包含的ip列表',
enable_referer_limiter tinyint not null comment '是否启用referer',
referer_limiter_include_referers varchar(2048) comment 'referer限制包含的referer列表',
enable_sync_limiter tinyint not null comment '是否启用同步限制',
enable_flow_limiter tinyint not null comment '是否启用流量限制',
flow_limiter_limit_count bigint comment '流量限制的数量',
enable_count_limiter tinyint not null comment '是否启用数量限制',
count_limiter_timeunit_pattern varchar(20) comment '数量限制的时间单位模式',
count_limiter_limit_count bigint comment '数量限制的数量',
enable_content_escape tinyint not null comment '是否启用内容转义',
enable_response_wrapper tinyint not null comment '是否启用响应包装',
content_escape_method_class varchar(500) comment '内容转义方式的类路径',
cors_limiter_configuration_class varchar(500) comment '跨域限制的类路径',
nonce_limiter_validator_class varchar(500) comment 'nonce限制校验器的类路径',
wrap_response_handler_class varchar(500) comment '包装返回值处理器的类路径'
)ENGINE=InnoDB COMMENT='能力支持列表';

create table apoollo_request_resource_matcher
(
id bigint primary key comment '主键id',
effect_on_type varchar(10) not null comment '生效的主体的类型',
effect_on_id bigint not null comment '生效的主体的id',
effect_on_target_type int not null comment '生效的主体目标的类型',
exclude_roles varchar(500) comment '排除角色列表',
exclude_ant_path_patterns varchar(1024) comment '排除ant路径列表',
exclude_request_resource_pins varchar(500) comment '排除资源pin列表',
include_roles varchar(500) comment '包含角色列表',
include_ant_path_patterns varchar(1024) comment '包含ant路径列表',
include_request_resource_pins varchar(500) comment '包含资源pin列表'
)ENGINE=InnoDB COMMENT='请求资源匹配器';