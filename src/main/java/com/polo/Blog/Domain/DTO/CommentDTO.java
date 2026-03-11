package com.polo.Blog.Domain.DTO;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommentDTO {
    @JsonSerialize(using = ToStringSerializer.class)
    private Long articleId;             // 文章Id
    private Long userId;                // 评论人Id
    private String userName;
    private String content;             // 评论内容
    private Long rootId;                // 根评论Id
    //private Long toCommentId;           // 回复评论Id
    private String toUserName;              // 回复用户昵称
    private Long toUserId;

}
