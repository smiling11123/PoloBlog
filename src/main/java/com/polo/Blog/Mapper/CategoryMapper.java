package com.polo.Blog.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.polo.Blog.Domain.Entity.Category;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CategoryMapper extends BaseMapper<Category> {
}
