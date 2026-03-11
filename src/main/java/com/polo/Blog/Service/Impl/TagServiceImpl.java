package com.polo.Blog.Service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.polo.Blog.Domain.Entity.Tag;
import com.polo.Blog.Mapper.TagMapper;
import com.polo.Blog.Service.TagService;
import com.polo.Blog.Utils.Result;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TagServiceImpl extends ServiceImpl<TagMapper, Tag> implements TagService {

    @Override
    public Result<List<Tag>> getTags(){
        return Result.success(this.list());
    }

    @Override
    public Result<String> createTag(String name){
        Tag tag = new Tag();
        tag.setName(name);
        tag.setCreateTime(LocalDateTime.now());
        this.save(tag);
        return Result.success("创建成功");
    }

    @Override
    public Result<String> updateTag(String name, Integer isDelete, Long id){
        LambdaQueryWrapper<Tag> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Tag::getId, id);
        Tag oldTag = this.getOne(wrapper);
        //处理特殊情况，阻止无效操作数据库
        if(isDelete.equals(0) && oldTag.getName().equals(name)) return Result.success("无需更改");
        Tag tag = new Tag();
        tag.setId(id);
        //防止删除时标签名丢失
        if(name != null && !name.isEmpty()){
            tag.setName(name);
        }
        tag.setIsDeleted(isDelete);
        tag.setUpdateTime(LocalDateTime.now());
        boolean isDone = this.updateById(tag);
        return isDone? Result.success("更新成功") : Result.fail(500, "更新失败");
    }

}
