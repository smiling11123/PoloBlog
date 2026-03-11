package com.polo.Blog.Service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.polo.Blog.Domain.Entity.Comment;
import com.polo.Blog.Domain.VO.CommentVO;
import com.polo.Blog.Utils.Result;

public interface CommentService extends IService<Comment> {


    /**
     * 发布评论
     * @param userId 评论人id
     * @param userName 评论人昵称
     * @param articleId 文章id
     * @param rootId 回复评论id
     * @param content 回复内容
     * @param toUserName 回复人昵称
     * @param toUserId 回复人id
     * @return 返回发布结果
     */
    Result<String> publishComment(Long userId, String userName, Long articleId, Long rootId, String content, String toUserName, Long toUserId);

    /**
     * 只查询 root 评论(rootId == null)
     * @param articleId 文章id
     * @param page 页数
     * @param size 页大小
     * @return 返回 N 条评论
     */
    Result<IPage<CommentVO>> getRootComment(Long articleId, Integer page, Integer size);

    /**
     * 查询子评论 (toCommentId == parentId)
     * @param rootId 父级评论id
     * @param page 页数
     * @param size 页大小
     * @return 返回 N 条子评论
     */
    Result<IPage<CommentVO>> getChildComment(Long rootId, Integer page, Integer size);
}
