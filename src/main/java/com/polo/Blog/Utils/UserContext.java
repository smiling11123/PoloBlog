package com.polo.Blog.Utils;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户上下文 - 基于 ThreadLocal
 */
public class UserContext {

    // 1. 定义 ThreadLocal，存放 LoginUser 对象
    private static final ThreadLocal<LoginUser> USER_HOLDER = new ThreadLocal<>();

    // 2. 存入用户信息
    public static void set(LoginUser user) {
        USER_HOLDER.set(user);
    }

    // 3. 获取用户信息
    public static LoginUser get() {
        return USER_HOLDER.get();
    }

    // 4. 清除用户信息 (防止内存泄漏，非常重要！)
    public static void remove() {
        USER_HOLDER.remove();
    }

    // --- 内部类：存储在 Context 中的数据结构 ---
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class LoginUser {
        private Long id; // 用户名
        private String roleKey;  // 角色标识 (admin, user)
        // 你还可以加 userId, nickname 等
    }
}

