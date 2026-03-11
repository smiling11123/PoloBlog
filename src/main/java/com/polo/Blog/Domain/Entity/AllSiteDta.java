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
@TableName("sys_all_site_data")
public class AllSiteDta {
    @TableId(type = IdType.ASSIGN_ID)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;                    // id
    private Integer totalUserCount;       // 所有用户
    private Integer totalArticleCount;    // 所有文章数
    private Integer totalCommentCount;    // 所有评论数
    private Long totalViewCount;        // 总文章浏览量
    private Long totalVisitCount;       // 总网站访问量
    private Long totalVisitorCount;     // 总独立访客数量
    private Long totalMessageSlipCount; // 留言数量
    private LocalDateTime updateTime;
}
