package com.polo.Blog.Controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.polo.Blog.Annootation.RequireAuth;
import com.polo.Blog.Domain.Entity.Wallpaper;
import com.polo.Blog.Service.WallpaperService;
import com.polo.Blog.Utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/wallpaper")
public class WallpaperController {
    @Autowired
    private WallpaperService wallpaperService;
    @GetMapping("list")
    public Result<IPage<Wallpaper>> getWallpaperList(@RequestParam (defaultValue = "1") Integer page, @RequestParam (defaultValue = "6") Integer size){
        return wallpaperService.getWallpaperList(page, size);
    }
    @RequireAuth
    @PostMapping("delete")
    public Result<String> deleteWallpaper(@RequestParam Long id){
        return wallpaperService.deleteWallpaper(id);
    }
    @RequireAuth
    @PostMapping("upload")
    public Result<String> uploadWallpaper(@RequestParam String name, @RequestParam("file") MultipartFile file){
        return wallpaperService.uploadWallpaper(file, name);
    }
}
