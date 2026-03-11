package com.polo.Blog.Service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.type.TypeReference;
import com.polo.Blog.Domain.DTO.WorksDTO;
import com.polo.Blog.Domain.Entity.Works;
import com.polo.Blog.Domain.VO.WorksVO;
import com.polo.Blog.Mapper.WorksMapper;
import com.polo.Blog.Service.WorksService;
import com.polo.Blog.Utils.EntityListToVOList;
import com.polo.Blog.Utils.RedisCache;
import com.polo.Blog.Utils.Result;
import com.polo.Blog.Utils.UserContext;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Service
public class WorksServiceImpl extends ServiceImpl<WorksMapper, Works> implements WorksService {

    @Autowired
    private RedisCache redisCache;

    @Override
    public Result<IPage<WorksVO>> getWorksList(Integer page, Integer size){
        String key = "works_" + page + "_" + size;
        IPage<WorksVO> worksIPageCache = redisCache.get(key, new TypeReference<IPage<WorksVO>>() {});
        if (worksIPageCache != null) return Result.success(worksIPageCache);

        IPage<Works> pageInfo = new Page<>(page, size);
        LambdaQueryWrapper<Works> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Works::getIsDeleted, 0);
        IPage<Works> worksIPage = this.page(pageInfo, wrapper);
        IPage<WorksVO> worksVOIPage = new Page<>();
        BeanUtils.copyProperties(worksIPage, worksVOIPage);

        worksVOIPage.setRecords(EntityListToVOList.worksListToVOList(worksIPage.getRecords()));
        redisCache.set(key, worksVOIPage, 5, TimeUnit.MINUTES);

        return Result.success(worksVOIPage);
    }
    @Override
    public Result<Works> getWorksDetail(Long id){
        String key = "works_" + id;
        Works worksCache = redisCache.get(key, Works.class);
        if(worksCache != null) return Result.success(worksCache);

        Works works = this.getById(id);

        redisCache.set(key, works, 30, TimeUnit.MINUTES);

        return Result.success(works);
    }

    @Override
    public Result<String> publishWorks(WorksDTO worksDTO){
        UserContext.LoginUser loginUser = UserContext.get();
        if(!loginUser.getRoleKey().equals("admin")) return Result.fail(403, "权限不足");
        Works works = new Works();
        BeanUtils.copyProperties(worksDTO, works);
        works.setCreateTime(LocalDateTime.now());

        this.save(works);
        return Result.success("发布成功");
    }
    @Override
    public Result<String> updateWorks(WorksDTO worksDTO){
        UserContext.LoginUser loginUser = UserContext.get();
        if(!loginUser.getRoleKey().equals("admin")) return Result.fail(403, "权限不足");

        Works works = this.getById(worksDTO.getId());
        BeanUtils.copyProperties(worksDTO, works);

        String key = "works_" + works.getId();
        redisCache.deleteCache(key);

        return Result.success("修改成功");
    }
    @Override
    public Result<String> deleteWorks(Long id){
        UserContext.LoginUser loginUser = UserContext.get();
        if(!loginUser.getRoleKey().equals("admin")) return Result.fail(403, "权限不足");

        Works works = this.getById(id);
        works.setIsDeleted(1);
        this.updateById(works);
        String key = "works_" + id;
        redisCache.deleteCache(key);
        return Result.success("删除成功");
    }
}
