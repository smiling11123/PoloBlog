package com.polo.Blog.Domain.VO;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Objects;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserVO {
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    private String username;
    private String nickname;
    private String email;
    private String avatar;
    private String intro;
    private String source;
    private String status;
    private String statusText;
    private String roleName;
    private String roleKey;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private LocalDateTime loginDate;
    public String getStatusText() {
        return Objects.equals(this.status, "0") ? "停用" : "正常";
    }

}
