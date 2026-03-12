package com.polo.Blog.Service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.polo.Blog.Domain.DTO.ArticleDTO;
import com.polo.Blog.Domain.Entity.Article;
import com.polo.Blog.Domain.VO.ArticleVO;
import com.polo.Blog.Utils.Result;


import java.util.List;

/**
 *
 */
public interface ArticleService extends IService<Article> {
    /**
     * 根据角色获取文章列表
     *
     * @return 返回文章列表
     */
    Result<IPage<ArticleVO>> getArticleList(int page, int size);

    /**
     * 获取回收站中的文章
     * @param page 页数
     * @param size 页大小
     * @return 返回一页
     */
    Result<IPage<ArticleVO>> getDeletedArticleList(int page, int size);

    /**
     * 恢复已删除文章
     * @param id 文章id
     * @return 返回恢复结果
     */
    Result<String> restoreArticle(Long id);

    /**
     * 根据文章id获取文章详细内容
     *
     * @param id 文章id
     * @return 返回一篇文章的详细信息
     */
    Result<ArticleVO> getArticleById(Long id);

    /**
     * 获取关键词搜索结果
     *
     * @param keyword 关键词（标题）
     * @return 返回对应列表
     */
    Result<IPage<ArticleVO>> getArticleByKeyWord(String keyword, List<Long> categoryId, int page, int size);

    /**
     * 获取对应标签分组
     *
     * @param tagId 标签组
     * @return 返回文章列表
     */
    Result<IPage<ArticleVO>> getArticleByTag(List<String> tagId, int page, int size);

    /**
     * 根据分类id获取该分类文章
     * @param categoryId 分类Id
     * @param page 页数
     * @param size 每页容量
     * @return 返回一页
     */
    Result<IPage<ArticleVO>> getArticleByCategory(Long categoryId, int page, int size);

    /**
     * 分页获取热门文章 根据浏览量
     *
     * @param page  页数
     * @param size 每页容量
     * @return 返回一页
     */
    Result<IPage<ArticleVO>> getHotArticle(int page, int size);

    /**
     * 分页获取草稿
     * @param page 页数
     * @param size 页大小
     * @return 返回一页
     */
    Result<IPage<ArticleVO>> getManuscriptList(int page, int size);

    /**
     * 管理员发布文章
     *
     * @param articleDTO 操作对象
     * @return 成功发布的消息
     */
    Result<String> publishArticle(ArticleDTO articleDTO);

    /**
     * 草稿发布
     * @param articleDTO 草稿
     * @return 发布成功信息
     */
    Result<String> manuscriptToArticle(ArticleDTO articleDTO);

    /**
     * 管理员更新文章
     * @param articleDTO 操作对象
     */
    Result<String> updateArticle(ArticleDTO articleDTO);

    /**
     * 删除文章
     *
     * @param id 操作对象id
     * @return 成功删除的消息（逻辑删除）
     */
    Result<String> deleteArticle(Long id);

    /**
     * 单篇文章每日归档增加浏览量
     * @param id 文章id
     * @param viewCount 浏览量
     */
    void updateViewCountDaily(Long id, Integer viewCount);

    /**
     * 切换文章评论状态 (0禁止 <-> 1允许)
     * @param id 文章id
     * @return 操作结果
     */
    Result<String> toggleCommentStatus(Long id);
}
