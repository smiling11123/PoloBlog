package com.polo.Blog.Domain.Entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("blog_article_daily_stats")
public class ArticleDaily {
    @TableId(type = IdType.ASSIGN_ID)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;                    // id
    private LocalDate date;             // 统计日期
    private Long articleId;             // 文章id
    private Integer viewCount;             // 浏览量
    private Integer likeCount;             // 点赞数
    private Integer commentCount;          // 评论数
    private LocalDateTime createTime;   // 创建时间

}
