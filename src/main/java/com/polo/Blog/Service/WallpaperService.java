package com.polo.Blog.Service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.polo.Blog.Annootation.RequireAuth;
import com.polo.Blog.Domain.Entity.Wallpaper;
import com.polo.Blog.Utils.Result;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

public interface WallpaperService extends IService<Wallpaper> {
    /**
     * 获取壁纸
     * @param page 页数
     * @param size 页大小
     * @return 返回一页
     */
    Result<IPage<Wallpaper>> getWallpaperList(Integer page, Integer size);

    /**
     * 获取已删除壁纸
     * @param page 页数
     * @param size 页大小
     * @return 返回一页
     */
    Result<IPage<Wallpaper>> getDeletedWallpaperList(Integer page, Integer size);

    /**
     * 删除壁纸
     * @param id 壁纸id
     * @return 返回处理结果
     */
    @RequireAuth
    Result<String> deleteWallpaper(Long id);

    /**
     * 恢复壁纸
     * @param id 壁纸id
     * @return 返回处理结果
     */
    Result<String> restoreWallpaper(Long id);

    /**
     * 上传壁纸
     * @param file 文件
     * @param name 文件名
     * @return 返回处理结果
     */
    @Transactional
    Result<String> uploadWallpaper(MultipartFile file, String name);
}
