package com.polo.Blog.Service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.polo.Blog.Domain.DTO.SloganDTO;
import com.polo.Blog.Domain.Entity.Slogan;
import com.polo.Blog.Utils.Result;

import java.util.List;

public interface SloganService extends IService<Slogan> {
    /**
     * 获取标语
     * @return 返回所有标语
     */
    Result<List<Slogan>> getSloganList();

    /**
     * 发布标语
     * @param sloganDTO 发布对象
     * @return 返回处理结果
     */
    Result<String> publishSlogan(SloganDTO sloganDTO);

    /**
     * 删除标语
     * @param id 删除对象id
     * @return 返回处理结果
     */
    Result<String> deleteSlogan(Long id);
}
