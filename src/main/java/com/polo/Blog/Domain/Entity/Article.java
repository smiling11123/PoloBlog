package com.polo.Blog.Domain.Entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("blog_article")
public class Article {
    @TableId(type = IdType.ASSIGN_ID)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;                         // 文章id
    private Long categoryId;                 // 所属分类ID
    private String title;                    // 文章标题
    private String summary;                  // 文章摘要
    private String content;                  // 文章内容
    private String thumbnail;                // 封面地址
    private Integer isTop;                   // 是否置顶(0否 1是)
    private Integer status;                  // 状态(0:草稿 1:发布)
    private Integer isComment;               // 是否允许评论(0否 1是)
    private Long viewCount;                  // 浏览量
    private Integer version;                     // 乐观锁版本号
    private LocalDateTime createTime;        // 创建时间
    private LocalDateTime updateTime;        // 更新时间
    private Long createBy;                   // 创建人ID
    private Integer isDeleted;               // 逻辑删除
}
