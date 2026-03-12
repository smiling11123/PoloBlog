package com.polo.Blog.Service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.type.TypeReference;
import com.polo.Blog.Domain.Entity.Wallpaper;
import com.polo.Blog.Mapper.WallpaperMapper;
import com.polo.Blog.Service.AssetStorageService;
import com.polo.Blog.Service.WallpaperService;
import com.polo.Blog.Utils.RedisCache;
import com.polo.Blog.Utils.Result;
import com.polo.Blog.Utils.UserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Objects;

@Service
public class WallpaperServiceImpl extends ServiceImpl<WallpaperMapper, Wallpaper> implements WallpaperService {
    @Autowired
    private RedisCache redisCache;
    @Autowired
    private AssetStorageService assetStorageService;
    @Override
    public Result<IPage<Wallpaper>> getWallpaperList(Integer page, Integer size){
       // String key = "WallpaperList_" + page + "_" + size;
       // Page<Wallpaper> wallpaperCache = redisCache.get(key, new TypeReference<Page<Wallpaper>>() {});
       // if(wallpaperCache != null) return wallpaperCache;

        Page<Wallpaper> wallpaperPage = new Page<>(page, size);
        LambdaQueryWrapper<Wallpaper> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Wallpaper::getIsDeleted, 0);
        Page<Wallpaper> wallpaperList = this.page(wallpaperPage, wrapper);

       // redisCache.set(key, wallpaperList, 2, TimeUnit.MINUTES);

        return Result.success(wallpaperList);
    }
    @Override
    public Result<IPage<Wallpaper>> getDeletedWallpaperList(Integer page, Integer size) {
        Page<Wallpaper> wallpaperPage = new Page<>(page, size);
        LambdaQueryWrapper<Wallpaper> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Wallpaper::getIsDeleted, 1).orderByDesc(Wallpaper::getCreateTime);
        return Result.success(this.page(wallpaperPage, wrapper));
    }
    @Override
    public Result<String> deleteWallpaper(Long id){
        UserContext.LoginUser loginUser = UserContext.get();
        if(!loginUser.getRoleKey().equals("admin")) return Result.fail(403, "权限不足");

//        LambdaQueryWrapper<Wallpaper> wrapper = new LambdaQueryWrapper<>();
//        wrapper.eq(Wallpaper::getId, id);
        Wallpaper wallpaper = this.getById(id);
        if (wallpaper == null || Objects.equals(wallpaper.getIsDeleted(), 1)) return Result.fail(404, "壁纸不存在");
        wallpaper.setIsDeleted(1);
        this.updateById(wallpaper);

        return Result.success("删除成功");
    }

    @Override
    public Result<String> restoreWallpaper(Long id) {
        UserContext.LoginUser loginUser = UserContext.get();
        if(!loginUser.getRoleKey().equals("admin")) return Result.fail(403, "权限不足");
        Wallpaper wallpaper = this.getById(id);
        if (wallpaper == null || Objects.equals(wallpaper.getIsDeleted(), 0)) return Result.fail(404, "壁纸不存在");
        wallpaper.setIsDeleted(0);
        this.updateById(wallpaper);
        return Result.success("恢复成功");
    }
    @Transactional
    @Override
    public Result<String> uploadWallpaper(MultipartFile file, String name){
        UserContext.LoginUser loginUser = UserContext.get();
        if(!Objects.equals(loginUser.getRoleKey(), "admin")) return Result.fail(403, "权限不足");
        if (file.isEmpty()) {
            return Result.fail(400, "上传文件不能为空");
        }

        try {
            String fileUrl = assetStorageService.store(file);

            Wallpaper wallpaper = new Wallpaper();
            wallpaper.setAddress(fileUrl);
            wallpaper.setName(name);
            wallpaper.setCreateTime(LocalDateTime.now());
            this.save(wallpaper);
            return Result.success(fileUrl);

        } catch (Exception e) {
            e.printStackTrace();
            return Result.fail(500, "MinIO上传失败: " + e.getMessage());
        }
    }
}
