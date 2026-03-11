package com.polo.Blog.Domain.Entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("sys_user") // 对应用户表
public class User {
    @TableId(type = IdType.ASSIGN_ID)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;                      // 用户id
    private String uuid;                  // 第三方平台的用户id
    private String username;              // 用户名
    private String nickname;              // 用户昵称
    private String password;
    private String email;                 // 用户邮箱地址
    private String avatar;                // 用户头像地址
    private String status;                // 用户状态
    private String source;             // 用户登录方式(github gitee)
    private LocalDateTime loginDate;      // 用户最近登录时间
    private LocalDateTime createTime;     // 用户创建时间
    private LocalDateTime updateTime;     // 用户更新时间
    private Integer isDeleted;   // 用户是否删除（逻辑删除  0：未删   1：已删）


}
