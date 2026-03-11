package com.polo.Blog.Service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.polo.Blog.Domain.Entity.ArticleDaily;
import com.polo.Blog.Utils.Result;

import java.util.List;

public interface ArticleDailyService extends IService<ArticleDaily> {

    /**
     * 获取一篇文章的最近 N 天的数据
     * @param articleId 文章id
     * @return 返回 N 条数据
     */
    Result<List<ArticleDaily>> getRecentArticleDaily(Long articleId);

    /**
     * 分页获取一篇文章的数据
     * @param articleId 文章id
     * @param page 页数
     * @param size 页大小
     * @return 返回一页
     */
    Result<IPage<ArticleDaily>> getArticleDaily(Long articleId, Integer page, Integer size);
}
