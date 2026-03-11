package com.polo.Blog.Domain.DTO;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ArticleDTO {
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;                // 文章id
    private String categoryName;    // 分类名
    private String title;           // 文章标题
    private String summary;         // 文章摘要
    private String content;         // 文章内容
    private String thumbnail;       // 封面地址
    private Integer isTop;          // 是否置顶(0否 1是)
    private Integer status;         // 状态(0:草稿 1:发布)
    private Integer isComment;      // 是否允许评论(0否 1是)
    private Integer isDeleted;
}
