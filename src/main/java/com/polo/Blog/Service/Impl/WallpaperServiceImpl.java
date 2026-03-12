package com.polo.Blog.Service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.type.TypeReference;
import com.polo.Blog.Domain.Entity.Wallpaper;
import com.polo.Blog.Mapper.WallpaperMapper;
import com.polo.Blog.Service.WallpaperService;
import com.polo.Blog.Utils.RedisCache;
import com.polo.Blog.Utils.Result;
import com.polo.Blog.Utils.UserContext;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class WallpaperServiceImpl extends ServiceImpl<WallpaperMapper, Wallpaper> implements WallpaperService {
    @Autowired
    private RedisCache redisCache;
    @Autowired
    private MinioClient minioClient; // 1. 注入 MinIO 客户端

    // 从配置文件读取桶名和地址，避免硬编码
    @Value("${minio.bucketName:myblog}")
    private String bucketName;

    @Value("${minio.publicEndpoint:${minio.endpoint:http://localhost:9000}}")
    private String publicEndpoint;
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
    public Result<String> deleteWallpaper(Long id){
        UserContext.LoginUser loginUser = UserContext.get();
        if(!loginUser.getRoleKey().equals("admin")) return Result.fail(403, "权限不足");

//        LambdaQueryWrapper<Wallpaper> wrapper = new LambdaQueryWrapper<>();
//        wrapper.eq(Wallpaper::getId, id);
        Wallpaper wallpaper = this.getById(id);
        wallpaper.setIsDeleted(1);
        this.updateById(wallpaper);

        return Result.success("删除成功");
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
            //生成新文件名
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String newFileName = UUID.randomUUID().toString() + extension;

            // 也就是把流通过网络发给 9000 端口
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)      // 桶名: myblog
                            .object(newFileName)     // 文件名
                            .stream(file.getInputStream(), file.getSize(), -1) // 文件流
                            .contentType(file.getContentType()) // 文件类型
                            .build()
            );

            // 格式: http://localhost:9000/travel/文件名
            String fileUrl = publicEndpoint + "/" + bucketName + "/" + newFileName;

            LambdaQueryWrapper<Wallpaper> wrapper = new LambdaQueryWrapper<>();
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
