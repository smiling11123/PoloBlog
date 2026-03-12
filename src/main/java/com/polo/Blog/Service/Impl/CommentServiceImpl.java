
package com.polo.Blog.Service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.polo.Blog.Domain.Entity.Article;
import com.polo.Blog.Domain.Entity.Comment;
import com.polo.Blog.Domain.Entity.User;
import com.polo.Blog.Domain.VO.CommentVO;
import com.polo.Blog.Mapper.CommentMapper;
import com.polo.Blog.Service.ArticleService;
import com.polo.Blog.Service.CommentService;
import com.polo.Blog.Service.UserService;
import com.polo.Blog.Utils.EntityListToVOList;
import com.polo.Blog.Utils.RedisCache;
import com.polo.Blog.Utils.Result;
import com.polo.Blog.Utils.UserContext;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements CommentService {
    @Autowired
    private ArticleService articleService;
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private RedisCache redisCache;
    @Autowired
    private UserService userService;
    @Override
    public Result<String> publishComment(Long articleId, Long rootId, String content, Long toUserId){
        UserContext.LoginUser loginUser = UserContext.get();
        if (loginUser == null || loginUser.getId() == null) {
            return Result.fail(401, "请登录后发表评论");
        }
        if (articleId == null) {
            return Result.fail(400, "文章不存在");
        }
        if (!StringUtils.hasText(content)) {
            return Result.fail(400, "评论内容不能为空");
        }
        content = content.trim();
        if (content.length() > 500) {
            return Result.fail(400, "评论内容不能超过500个字符");
        }
        Article article = articleService.getById(articleId);
        if(article == null) return Result.fail(404, "文章不存在");
        if (article.getIsComment() != null && article.getIsComment() == 0) {
            return Result.fail(400, "当前文章暂未开放评论");
        }
        User currentUser = userService.getById(loginUser.getId());
        if (currentUser == null || Integer.valueOf(1).equals(currentUser.getIsDeleted())) {
            return Result.fail(404, "当前用户不存在");
        }
        String userName = StringUtils.hasText(currentUser.getNickname()) ? currentUser.getNickname() : currentUser.getUsername();

        Comment comment = new Comment();
        comment.setContent(content);
        comment.setArticleId(articleId);
        comment.setRootId(rootId == null ? -1L : rootId);
        comment.setUserId(currentUser.getId());
        comment.setCreateTime(LocalDateTime.now());
        comment.setUserName(userName);
        if(comment.getRootId() != -1){
            comment.setToUserId(toUserId);
            if (toUserId != null) {
                User toUser = userService.getById(toUserId);
                if (toUser != null) {
                    String toUserName = StringUtils.hasText(toUser.getNickname()) ? toUser.getNickname() : toUser.getUsername();
                    comment.setToUserName(toUserName);
                }
            }
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
        wrapper.eq(Comment::getArticleId, articleId).eq(Comment::getRootId, -1).orderByDesc(Comment::getCreateTime);
        this.page(pageInfo, wrapper);

        IPage<CommentVO> commentVOIPage = new Page<>();
        BeanUtils.copyProperties(pageInfo, commentVOIPage);
        List<CommentVO> rootCommentList = EntityListToVOList.commentListToVOList(pageInfo.getRecords());
        fillCommentDisplayInfo(rootCommentList);
        commentVOIPage.setRecords(rootCommentList);
        commentVOIPage.getRecords().forEach(commentVO -> {
            LambdaQueryWrapper<Comment> childWrapper = new LambdaQueryWrapper<>();
            childWrapper.eq(Comment::getRootId, commentVO.getId());
            commentVO.setChildCount((int) this.count(childWrapper));
        });
//        redisCache.set(key, commentVOIPage, 30, TimeUnit.MINUTES);
        return Result.success(commentVOIPage);
    }

    @Override
    public Result<IPage<CommentVO>> getChildComment(Long rootId, Integer page, Integer size){
        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<>();
        Page<Comment> pageInfo = new Page<>(page, size);
        //toCommentId == parentId 的评论
        wrapper.eq(Comment::getRootId, rootId).orderByAsc(Comment::getCreateTime);
        this.page(pageInfo, wrapper);

        IPage<CommentVO> commentVOIPage = new Page<>();
        BeanUtils.copyProperties(pageInfo, commentVOIPage);
        List<CommentVO> childCommentList = EntityListToVOList.commentListToVOList(pageInfo.getRecords());
        fillCommentDisplayInfo(childCommentList);
        return Result.success(commentVOIPage.setRecords(childCommentList));
    }

    private void fillCommentDisplayInfo(List<CommentVO> comments) {
        if (comments == null || comments.isEmpty()) {
            return;
        }

        Set<Long> userIds = comments.stream()
                .map(CommentVO::getUserId)
                .filter(id -> id != null && id > 0)
                .collect(Collectors.toSet());
        if (userIds.isEmpty()) {
            return;
        }

        LambdaQueryWrapper<User> userWrapper = new LambdaQueryWrapper<>();
        userWrapper.in(User::getId, userIds).eq(User::getIsDeleted, 0);
        Map<Long, User> userMap = userService.list(userWrapper).stream()
                .collect(Collectors.toMap(User::getId, user -> user, (left, right) -> left));

        comments.forEach(commentVO -> {
            User user = userMap.get(commentVO.getUserId());
            if (user == null) {
                return;
            }
            commentVO.setUserAvatar(user.getAvatar());
            if (!StringUtils.hasText(commentVO.getUserName())) {
                String displayName = StringUtils.hasText(user.getNickname()) ? user.getNickname() : user.getUsername();
                commentVO.setUserName(displayName);
            }
        });
    }
}
