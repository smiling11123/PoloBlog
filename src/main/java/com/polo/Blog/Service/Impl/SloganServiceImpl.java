package com.polo.Blog.Service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.type.TypeReference;
import com.polo.Blog.Domain.DTO.SloganDTO;
import com.polo.Blog.Domain.Entity.Slogan;
import com.polo.Blog.Mapper.SloganMapper;
import com.polo.Blog.Service.SloganService;
import com.polo.Blog.Utils.RedisCache;
import com.polo.Blog.Utils.Result;
import com.polo.Blog.Utils.UserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Service
public class SloganServiceImpl extends ServiceImpl<SloganMapper, Slogan> implements SloganService {
    private static final String SLOGAN_CACHE_KEY = "slogans";

    @Autowired
    private RedisCache redisCache;

    @Override
    public Result<List<Slogan>> getSloganList(){
        List<Slogan> slogansCache = redisCache.get(SLOGAN_CACHE_KEY, new TypeReference<List<Slogan>>() {});
        if(slogansCache != null) return Result.success(slogansCache);

        LambdaQueryWrapper<Slogan> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Slogan::getIsDeleted, 0)
                .orderByDesc(Slogan::getCreateTime);
        List<Slogan> slogans = this.list(wrapper);

        redisCache.set(SLOGAN_CACHE_KEY, slogans, 30, TimeUnit.MINUTES);

        return Result.success(slogans);
    }

    @Override
    public Result<String> publishSlogan(SloganDTO sloganDTO){
        UserContext.LoginUser loginUser = UserContext.get();
        if(!loginUser.getRoleKey().equals("admin")) return Result.fail(401, "请登录后操作");
        if(sloganDTO == null || sloganDTO.getContent() == null || sloganDTO.getContent().trim().isEmpty()) {
            return Result.fail(400, "标语内容不能为空");
        }

        String content = sloganDTO.getContent().trim();
        if(content.length() > 128) {
            return Result.fail(400, "标语内容不能超过128个字符");
        }
        LambdaQueryWrapper<Slogan> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Slogan::getContent, content)
                .last("limit 1");
        Slogan existSlogan = this.getOne(wrapper);
        if(existSlogan != null && Objects.equals(existSlogan.getIsDeleted(), 0)) {
            return Result.fail(400, "该标语已存在");
        }
        if(existSlogan != null) {
            existSlogan.setIsDeleted(0);
            existSlogan.setCreateTime(LocalDateTime.now());
            boolean updated = this.updateById(existSlogan);
            if(!updated) {
                return Result.fail(500, "发布失败");
            }
            redisCache.deleteCache(SLOGAN_CACHE_KEY);
            return Result.success("发布成功");
        }

        Slogan slogan = new Slogan();
        slogan.setContent(content);
        slogan.setCreateTime(LocalDateTime.now());
        slogan.setIsDeleted(0);

        boolean saved = this.save(slogan);
        if(!saved) {
            return Result.fail(500, "发布失败");
        }

        redisCache.deleteCache(SLOGAN_CACHE_KEY);

        return Result.success("发布成功");
    }

    @Override
    public Result<String> deleteSlogan(Long id){
        UserContext.LoginUser loginUser = UserContext.get();
        if(!loginUser.getRoleKey().equals("admin")) return Result.fail(401, "请登录后操作");

        Slogan slogan = this.getById(id);
        if(Objects.isNull(slogan) || Objects.equals(slogan.getIsDeleted(), 1)) {
            return Result.fail(404, "标语不存在");
        }
        slogan.setIsDeleted(1);

        boolean updated = this.updateById(slogan);
        if(!updated) {
            return Result.fail(500, "删除失败");
        }

        redisCache.deleteCache(SLOGAN_CACHE_KEY);

        return Result.success("删除成功");
    }
}
