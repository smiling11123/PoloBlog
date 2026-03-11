package com.polo.Blog.Domain.Entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("blog_comment")
public class Comment {
    @TableId(type = IdType.ASSIGN_ID)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;                    //
    private Long articleId;             // 文章Id
    private Long userId;                // 评论人Id
    private String userName;
    private String content;             // 评论内容
    private Long rootId;                // 根评论Id
    //private Long toCommentId;           // 回复评论Id
    private Long toUserId;              // 回复用户Id
    private String toUserName;
    private Integer likeCount;          // 点赞数
    private String status;             //
    private LocalDateTime createTime;   //
    private LocalDateTime updateTime;   //
    private Integer isDeleted;          //
}
