package com.polo.Blog.Domain.DTO;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryDTO {
    @JsonSerialize(using = ToStringSerializer.class)
    private String name;                // 分类名
    private Long pid;                   // 父类Id
    private String description;         // 分类描述
    private Integer sort;               // 排序
    private Long viewCount;             //浏览量
    private String thumbnail;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer isDeleted;
}
