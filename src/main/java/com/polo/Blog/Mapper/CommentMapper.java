package com.polo.Blog.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.polo.Blog.Domain.Entity.Comment;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CommentMapper extends BaseMapper<Comment> {
}
