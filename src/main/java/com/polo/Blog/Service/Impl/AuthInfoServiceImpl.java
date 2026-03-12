package com.polo.Blog.Service.Impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.polo.Blog.Domain.DTO.AuthInfoDTO;
import com.polo.Blog.Domain.Entity.AuthInfo;
import com.polo.Blog.Mapper.AuthInfoMapper;
import com.polo.Blog.Service.AuthInfoService;
import com.polo.Blog.Utils.RedisCache;
import com.polo.Blog.Utils.Result;
import com.polo.Blog.Utils.UserContext;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Service
public class AuthInfoServiceImpl extends ServiceImpl<AuthInfoMapper, AuthInfo> implements AuthInfoService {
    @Autowired
    private RedisCache redisCache;

    @Override
    public Result<AuthInfo> getAuthInfo() {
        String key = "authInfo";
        AuthInfo authInfoCache = redisCache.get(key, AuthInfo.class);
        if (authInfoCache != null) {
            return Result.success(authInfoCache);
        }

        AuthInfo authInfo = getOrCreateAuthInfo();
        redisCache.set(key, authInfo, 30, TimeUnit.MINUTES);
        return Result.success(authInfo);
    }

    @Override
    public Result<String> updateAuthInfo(AuthInfoDTO authInfoDTO) {
        UserContext.LoginUser loginUser = UserContext.get();
        if (!loginUser.getRoleKey().equals("admin")) {
            return Result.fail(403, "权限不足");
        }

        AuthInfo authInfo = getOrCreateAuthInfo();
        BeanUtils.copyProperties(authInfoDTO, authInfo);
        authInfo.setUpdateTime(LocalDateTime.now());
        this.updateById(authInfo);

        String key = "authInfo";
        redisCache.deleteCache(key);
        return Result.success("更新成功");
    }

    private AuthInfo getOrCreateAuthInfo() {
        AuthInfo authInfo = this.getById(1L);
        if (authInfo != null) {
            return authInfo;
        }

        AuthInfo defaultAuthInfo = new AuthInfo();
        defaultAuthInfo.setId(1L);
        defaultAuthInfo.setUserName("博主");
        defaultAuthInfo.setAvatar("");
        defaultAuthInfo.setProfile("这个人很懒，还没有填写简介。");
        defaultAuthInfo.setUpdateTime(LocalDateTime.now());
        this.save(defaultAuthInfo);
        return defaultAuthInfo;
    }
}
