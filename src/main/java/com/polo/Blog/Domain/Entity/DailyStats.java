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
@TableName("sys_daily_statistics")
public class DailyStats {
    @TableId(type = IdType.ASSIGN_ID)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;                    // id
    private LocalDate date;             // 统计日期
    private Integer loginUserCount;     // 登录用户
    private Integer newArticleCount;    // 新发布文章数
    private Integer newCommentCount;    // 新评论数
    private Long totalViewCount;        // 文章浏览量
    private Long totalVisitCount;       // 网站访问量
    private Long totalVisitorCount;     // 独立访客数量
    private Long MessageSlipCount;      // 留言数量
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

}
