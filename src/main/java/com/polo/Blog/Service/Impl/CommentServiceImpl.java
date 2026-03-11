
package com.polo.Blog.Service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.polo.Blog.Domain.Entity.Article;
import com.polo.Blog.Domain.Entity.Comment;
import com.polo.Blog.Domain.VO.CommentVO;
import com.polo.Blog.Mapper.CommentMapper;
import com.polo.Blog.Service.ArticleService;
import com.polo.Blog.Service.CommentService;
import com.polo.Blog.Utils.EntityListToVOList;
import com.polo.Blog.Utils.RedisCache;
import com.polo.Blog.Utils.Result;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements CommentService {
    @Autowired
    private ArticleService articleService;
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private RedisCache redisCache;
    @Override
    public Result<String> publishComment(Long userId, String userName, Long articleId, Long rootId, String content, String toUserName, Long toUserId){
        Article article = articleService.getById(articleId);
        if(article == null) return Result.fail(404, "文章不存在");
        Comment comment = new Comment();
        comment.setContent(content);
        comment.setArticleId(articleId);
        comment.setRootId(rootId);
        comment.setUserId(userId);
        comment.setCreateTime(LocalDateTime.now());
        comment.setUserName(userName);
        if(rootId != -1){
            comment.setToUserId(toUserId);
            comment.setToUserName(toUserName);
        }
        this.save(comment);

        //每日统计
        String today = LocalDate.now().toString();
        //全站评论统计
        String commentCountTotalKey = "sys_daily_statistics:" + today + ":new_comment_count";
        redisTemplate.opsForValue().increment(commentCountTotalKey);
        //单篇文章统计
        String commentCountPerKey = "blog_article_daily_stats:" + today + ":comment_count";
        redisTemplate.opsForHash().increment(commentCountPerKey, String.valueOf(comment.getArticleId()), 1);
        return Result.success("发布成功");
    }

    @Override
    public Result<IPage<CommentVO>> getRootComment(Long articleId, Integer page, Integer size){
//        /// 查Redis
//        String key = "Comment_Root_Article_" + articleId + "_" + page + "_" + size;
//        IPage<CommentVO> commentVOIPageCache = redisCache.get(key, new TypeReference<IPage<CommentVO>>() {});
//        if(commentVOIPageCache != null) return commentVOIPageCache;
//        /// redis未命中
        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<>();
        Page<Comment> pageInfo = new Page<>(page, size);
        //获取rootId == null 的评论
        wrapper.eq(Comment::getArticleId, articleId).eq(Comment::getRootId, -1);
        this.page(pageInfo, wrapper);

        IPage<CommentVO> commentVOIPage = new Page<>();
        BeanUtils.copyProperties(pageInfo, commentVOIPage);
        commentVOIPage.setRecords(EntityListToVOList.commentListToVOList(pageInfo.getRecords()));
//        redisCache.set(key, commentVOIPage, 30, TimeUnit.MINUTES);
        return Result.success(commentVOIPage);
    }

    @Override
    public Result<IPage<CommentVO>> getChildComment(Long rootId, Integer page, Integer size){
        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<>();
        Page<Comment> pageInfo = new Page<>(page, size);
        //toCommentId == parentId 的评论
        wrapper.eq(Comment::getRootId, rootId);
        this.page(pageInfo, wrapper);

        IPage<CommentVO> commentVOIPage = new Page<>();
        BeanUtils.copyProperties(pageInfo, commentVOIPage);

        return Result.success(commentVOIPage.setRecords(EntityListToVOList.commentListToVOList(pageInfo.getRecords())));
    }

}
