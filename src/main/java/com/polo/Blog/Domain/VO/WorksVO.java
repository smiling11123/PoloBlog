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
public class WorksVO {
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    private String title;
    private String thumbnail;
    private String address;
    private Integer isDeleted;
    private LocalDateTime createTime;
}