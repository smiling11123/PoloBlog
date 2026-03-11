package com.polo.Blog.Domain.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginUserDTO {
    private String uuid;         //
    private String username;     // 用户名
    private String nickname;     // 用户昵称
    private String email;        // 用户邮箱地址
    private String avatar;       // 头像
    private String source;    // 用户登录方式
}
