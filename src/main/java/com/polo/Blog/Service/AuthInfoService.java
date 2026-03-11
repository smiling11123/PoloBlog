package com.polo.Blog.Service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.polo.Blog.Domain.DTO.AuthInfoDTO;
import com.polo.Blog.Domain.Entity.AuthInfo;
import com.polo.Blog.Utils.Result;

public interface AuthInfoService extends IService<AuthInfo> {
    /**
     * 获取作者简介
     * @return 返回
     */
    Result<AuthInfo> getAuthInfo();

    /**
     * 更新作者简介
     * @param authInfoDTO 新数据
     * @return 返回处理结果
     */
    Result<String> updateAuthInfo(AuthInfoDTO authInfoDTO);
}
