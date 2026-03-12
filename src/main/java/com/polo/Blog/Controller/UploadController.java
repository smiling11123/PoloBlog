package com.polo.Blog.Controller;

import com.polo.Blog.Annootation.RequireAuth;
import com.polo.Blog.Service.AssetStorageService;
import com.polo.Blog.Utils.Result;
import com.polo.Blog.Utils.UserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/upload")
public class UploadController {

    @Autowired
    private AssetStorageService assetStorageService;
    @RequireAuth
    @PostMapping("/file")
    public Result upload(@RequestParam("file") MultipartFile file) {
        UserContext.LoginUser loginUser = UserContext.get();
        if(!loginUser.getRoleKey().equals("admin")) return Result.error("权限不足");
        if (file.isEmpty()) {
            return Result.error("上传文件不能为空");
        }

        try {
            return Result.success(assetStorageService.store(file));

        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("MinIO上传失败: " + e.getMessage());
        }
    }
}
