package com.polo.Blog.Domain.VO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommentVO {
    private Long id;                    //
    private Long articleId;             // 文章Id
    private Long userId;                // 评论人Id
    private String userName;            // 评论人昵称
    private String userAvatar;          // 评论人头像
    private String content;             // 评论内容
    private Long rootId;                // 根评论Id
    //private Long toCommentId;           // 回复评论Id
    private Long toUserId;              // 回复用户Id
    private String toUserName;
    private Integer likeCount;          // 点赞数
    private Integer childCount;         // 回复数量
    private Integer showReplies;        // 是否有回复
    private LocalDateTime createTime;   //
}
