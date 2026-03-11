package com.polo.Blog.Service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.type.TypeReference;
import com.polo.Blog.Domain.Entity.ArticleDaily;
import com.polo.Blog.Mapper.ArticleDailyMapper;
import com.polo.Blog.Service.ArticleDailyService;
import com.polo.Blog.Utils.RedisCache;
import com.polo.Blog.Utils.Result;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class ArticleDailyServiceImpl extends ServiceImpl<ArticleDailyMapper, ArticleDaily> implements ArticleDailyService {
    @Autowired
    private RedisCache redisCache;
    @Override
    public Result<List<ArticleDaily>> getRecentArticleDaily(Long articleId){
        /// redis缓存
        String key = "ArticleDailyCache_" + articleId;
        List<ArticleDaily> articleDailyCache = redisCache.get(key, new TypeReference<List<ArticleDaily>>() {});
        if(articleDailyCache != null) return Result.success(articleDailyCache);

        LambdaQueryWrapper<ArticleDaily> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ArticleDaily::getArticleId, articleId);
        //取近30天的数据做趋势图
        wrapper.orderByDesc(ArticleDaily::getDate).last("LIMIT 30");
        List<ArticleDaily> articleDaily = this.list(wrapper);

        redisCache.set(key, articleDaily, 23, TimeUnit.HOURS);

        return Result.success(articleDaily);
    }

    @Override
    public Result<IPage<ArticleDaily>> getArticleDaily(Long articleId, Integer page, Integer size){
        String key = "ArticleDailyCache_" + articleId + "_" + page + "_" + size;
        IPage<ArticleDaily> articleDailyCache = redisCache.get(key, new TypeReference<IPage<ArticleDaily>>() {});
        if(articleDailyCache != null) return Result.success(articleDailyCache);
        LambdaQueryWrapper<ArticleDaily> wrapper = new LambdaQueryWrapper<>();
        Page<ArticleDaily> pageInfo = new Page<>(page, size);
        wrapper.eq(ArticleDaily::getArticleId, articleId);
        //取近30天的数据做趋势图
        wrapper.orderByDesc(ArticleDaily::getDate);
        Page<ArticleDaily> articleDailyPage = this.page(pageInfo, wrapper);

        redisCache.set(key, articleDailyPage, 23, TimeUnit.HOURS);

        return Result.success(this.page(pageInfo, wrapper));
    }
}
