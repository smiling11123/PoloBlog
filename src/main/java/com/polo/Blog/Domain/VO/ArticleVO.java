package com.polo.Blog.Domain.VO;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ArticleVO {
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;                         // 文章id
    private Long categoryId;                 // 所属分类ID
    private String categoryName;             // 分类名
    private String title;                    // 文章标题
    private String summary;                  // 文章摘要
    private String thumbnail;                // 封面地址
    private String content;                  // 文章内容
    private Integer isTop;                   // 是否置顶(0否 1是)
    private Integer status;                  // 状态(0:草稿 1:发布)
    private Integer isComment;                // 是否允许评论(0:禁止 1:允许)
    private Long viewCount;                  // 浏览量
    private LocalDateTime createTime;        // 创建时间
    private LocalDateTime updateTime;        // 更新时间
    private Long createBy;                   // 创建人ID
    private String auth;                     // 作者
    private Integer isDeleted;               // 逻辑删除
}
