create apoollo_user
(
id bigint primary key comment '主键id',
user_pin varchar(32) unique key not null comment '用户唯一标识',
user_name varchar(50) unique key not null comment '用户名称',
)ENGINE=InnoDB COMMENT='用户';


create apoollo_user_key
(
id bigint primary key comment '主键id',
access_key char(32) unique key not null comment 'key唯一标识',
secret_key char(32) unique key not null comment 'key秘钥',
enabled tinyint
)ENGINE=InnoDB COMMENT='用户';

create apoollo_role
(
id bigint primary key comment '主键id',
role_pin varchar(32) unique key not null comment '角色唯一标识',
role_name varchar(50) unique key not null comment '角色名称'
)ENGINE=InnoDB COMMENT='角色';