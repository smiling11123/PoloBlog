package com.polo.Blog.Domain.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthInfoDTO {
    private String userName;
    private String avatar;              //头像
    private String profile;             //简介
    private String slogan;              //标语
}
