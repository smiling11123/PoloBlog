package com.polo.Blog.Utils;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;

/**
 * 统一封装响应结果
 * @param <T> 类型
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
// 统一数据返回结果
public class Result<T> {
    private int code;
    private String msg;
    private T data;

    public static <T> Result<T> success(T data) {
        Result<T> res = new Result<>();
        res.setCode(200);
        res.setMsg("成功响应");
        res.setData(data);
        return res;
    }

    public static <T> Result<T> error(String error) {
        Result<T> res = new Result<>();
        res.setCode(500);
        res.setMsg(error);
        res.setData(null);
        return res;
    }

    public static <T> Result<T> fail(int code, String msg) {
        return new Result<>(code, msg, null);
    }
}
