package com.polo.Blog.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.polo.Blog.Domain.Entity.Article;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ArticleMapper extends BaseMapper<Article> {
}
