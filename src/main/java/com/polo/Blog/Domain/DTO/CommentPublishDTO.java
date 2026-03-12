package com.polo.Blog.Domain.DTO;

import lombok.Data;

@Data
public class CommentPublishDTO {
    private Long articleId;
    private Long rootId = -1L;
    private String content;
    private Long toUserId;
}
