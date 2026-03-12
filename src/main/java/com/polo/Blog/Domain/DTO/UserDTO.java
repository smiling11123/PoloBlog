package com.polo.Blog.Domain.DTO;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDTO {
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;             // id
    private String username;     // 用户名
    private String nickname;     // 用户昵称
    private String email;        // 用户邮箱地址
    private String avatar;       // 头像
    private String intro;        // 用户简介
    private String source;    // 用户登录方式
    private String status;       // 用户状态
    private Integer isDeleted;   // 逻辑删除
    private String password;
}
