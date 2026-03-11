package com.polo.Blog.Service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.polo.Blog.Domain.Entity.Tag;
import com.polo.Blog.Utils.Result;

import java.util.List;

public interface TagService extends IService<Tag> {
    /**
     * 获取所有标签
     * @return 返回所有标签
     */
    Result<List<Tag>> getTags();

    /**
     * 创建标签
     * @param name 标签名
     * @return 返回创建成功
     */
    Result<String> createTag(String name);

    /**
     * 更新标签
     * @param name 新标签名
     * @param isDelete 是否删除（默认为0 不删除）
     * @param id 更新的标签id
     * @return 返回更新成功
     */
    Result<String> updateTag(String name, Integer isDelete, Long id);
}
