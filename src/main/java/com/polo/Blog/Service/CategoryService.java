package com.polo.Blog.Service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.polo.Blog.Domain.DTO.CategoryDTO;
import com.polo.Blog.Domain.Entity.Category;
import com.polo.Blog.Utils.Result;

import java.util.List;
import com.baomidou.mybatisplus.core.metadata.IPage;

public interface CategoryService extends IService<Category> {
    /**
     *  根据创建时间获取前10个分类
     * @return 10个分类
     */
    Result<List<Category>> getHotCategoryList();

    /**
     * 获取所有分类
     * @return 返回所有分类
     */
    Result<List<Category>> getCategoryList();

    /**
     * 获取已删除分类
     * @param page 页数
     * @param size 页大小
     * @return 返回一页
     */
    Result<IPage<Category>> getDeletedCategoryList(int page, int size);

    /**
     * 创建分类
     * @param categoryDTO 创建对象
     * @return 返回处理结果
     */
    Result createCategory(CategoryDTO categoryDTO);

    /**
     * 更新浏览量
     * @param id 分类id
     * @param viewCount 新增浏览量
     */
    void updateViewCount(Long id, Integer viewCount);

    /**
     * 删除分类
     * @param id 分类id
     * @return 返回处理结果
     */
    Result deleteCategory(Long id);

    /**
     * 恢复分类
     * @param id 分类id
     * @return 返回处理结果
     */
    Result restoreCategory(Long id);
}
