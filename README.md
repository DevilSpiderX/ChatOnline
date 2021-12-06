<h1 style="text-align : center;" align="center">数据库课设</h1>
<p style="text-align : center;" align="center">在线聊天系统</p>

# 配置文件

- PORT 端口号
- WEB_DIRECTORY 网页文件目录
- LOG_MAX_NUMBER 日志最大数量
- ERROR_PAGE 错误页面
    - CODE 错误代码
    - LOCATION 错误页面文件地址(以 / 开头，相对于网页文件目录的路径)

```json
{
  "PORT": 80,
  "WEB_DIRECTORY": "C:/Web",
  "LOG_MAX_NUMBER": 1,
  "ERROR_PAGE": [
    {
      "CODE": 404,
      "LOCATION": "/error/404.html"
    },
    {
      "CODE": 500,
      "LOCATION": "/error/500.html"
    }
  ]
}
```

# API接口文档

- [点击这里 - 文档](https://github.com/DevilSpiderX/ChatOnline/tree/main/doc)

# 依赖

- [bee-1.9.5](https://gitee.com/automvc/bee)
- [mysql-connector-java-8.0.22](https://github.com/mysql/mysql-connector-j)
- [fastjson-1.2.76](https://github.com/alibaba/fastjson)
- [tomcat-9.0.54](https://github.com/apache/tomcat)

# 数据库

数据库使用的是mysql

**表：**

- 主键使用<u>下划线</u>标出，外键使用#标出

| 表名             |    字段1     | 字段2      | 字段3         | 字段4  | 字段5         | 字段6           |
|:---------------|:----------:|----------|-------------|------|-------------|---------------|
| user           | <u>uid</u> | password | nickname    | age  | gender      | introduction  |
| friends        | <u>id</u>  | #own_uid | #friend_uid |      |             |               |
| message_record | <u>id</u>  | message  | state       | time | #sender_uid | #receiver_uid |

**视图：**

| 视图名                 | 字段1 | 字段2     | 字段3        | 字段4     | 字段5   | 字段6  | 字段7        |
|---------------------|-----|---------|------------|---------|-------|------|------------|
| friend_message_view | id  | own_uid | friend_uid | message | state | time | sender_uid |

```sql
CREATE VIEW `friend_message_view` AS 
SELECT 
  `friends`.`own_uid` AS `own_uid`,
  `friends`.`friend_uid` AS `friend_uid`,
  `message_record`.`message` AS `message`,
  `message_record`.`state` AS `state`,
  `message_record`.`time` AS `time`,
  `message_record`.`sender_uid` AS `sender_uid` 
FROM (`message_record` 
JOIN `friends` 
ON((((
        `message_record`.`sender_uid` = `friends`.`own_uid`
    ) 
    AND (
        `message_record`.`receiver_uid` = `friends`.`friend_uid`
    )) 
    OR((
        `message_record`.`sender_uid` = `friends`.`friend_uid`
    ) 
    AND(
        `message_record`.`receiver_uid` = `friends`.`own_uid`
    )))))
```

**存储过程：**

```sql
CREATE PROCEDURE `add_user`(IN `in_uid` varchar(20),IN `in_password` varchar(255),IN `in_nickname` varchar(255),
                            IN `in_age` int,IN `in_gender` char(1),IN `in_introduction` varchar(255))
BEGIN
	INSERT INTO `user`
	VALUES(`in_uid`,`in_password`,`in_nickname`,`in_age`,`in_gender`,`in_introduction`);
END
```