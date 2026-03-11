package com.polo.Blog.Service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.type.TypeReference;
import com.polo.Blog.Domain.DTO.CategoryDTO;
import com.polo.Blog.Domain.Entity.Category;
import com.polo.Blog.Mapper.CategoryMapper;
import com.polo.Blog.Service.CategoryService;
import com.polo.Blog.Utils.RedisCache;
import com.polo.Blog.Utils.Result;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {
    @Autowired
    private RedisCache redisCache;

    @Override
    public Result<List<Category>> getHotCategoryList(){
        /// 查redis
        String key = "HotCategoryList";
        List<Category> categoryCacheList = redisCache.get(key, new TypeReference<List<Category>>() {});
        if(categoryCacheList != null) return Result.success(categoryCacheList);
        /// redis 未命中
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Category::getIsDeleted, '0');
        wrapper.orderByDesc(Category::getViewCount);
        List<Category> categoryList = this.list(wrapper);
        List<Category> categoryHotList = categoryList.size() > 3 ? categoryList.subList(0, 3) : categoryList;
        /// 写入缓存
        redisCache.set(key, categoryHotList, 30, TimeUnit.MINUTES);
        return Result.success(categoryHotList);
    }

    @Override
    public Result<List<Category>> getCategoryList(){
        String key = "categoryList";
        List<Category> categoryListCache = redisCache.get(key, new TypeReference<List<Category>>() {});
        if(categoryListCache != null) return Result.success(categoryListCache);

        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Category::getIsDeleted, '0');
        List<Category> categoryList = this.list(wrapper);

        redisCache.set(key, categoryList, 30, TimeUnit.MINUTES);

        return Result.success(categoryList);
    }
    @Override
    public Result createCategory(CategoryDTO categoryDTO){
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Category::getName, categoryDTO.getName());
        Category category = this.getOne(wrapper);
        if(category != null) return new Result<>(400, "已有该分类", null);

        Category newCategory = new Category();
        BeanUtils.copyProperties(categoryDTO, newCategory);

        this.save(newCategory);

        /// 清除缓存
        redisCache.deleteCache("categoryList");
        redisCache.deleteCache("HotCategoryList");

        return new Result<>(200, "创建成功", null);
    }
    @Override
    public void updateViewCount(Long id, Integer viewCount){
        LambdaUpdateWrapper<Category> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(Category::getId, id).setSql("view_count = view_count + " + viewCount);
        //第一个参数null 表示不更新其他字段
        this.update(null, wrapper);
    }

    @Override
    public Result deleteCategory(Long id) {
        Category category = this.getById(id);
        if (category == null) return Result.fail(400, "分类不存在");
        
        category.setIsDeleted(1);
        this.updateById(category);

        /// 清除缓存
        redisCache.deleteCache("categoryList");
        redisCache.deleteCache("HotCategoryList");

        return Result.success("删除成功");
    }
}
