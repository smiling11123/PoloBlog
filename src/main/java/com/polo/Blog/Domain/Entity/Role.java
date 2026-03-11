package com.polo.Blog.Domain.Entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("sys_role")
public class Role {
    @TableId(type = IdType.ASSIGN_ID)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;                    //角色id
    private String roleName;            //角色名
    private String roleKey;             //角色权限名（admin， user， auth）
    private String status;              //角色状态
    private LocalDateTime createTime;   //角色创建时间
    private LocalDateTime updateTime;   //角色更新时间
    private Integer isDeleted;          //逻辑删除
}
