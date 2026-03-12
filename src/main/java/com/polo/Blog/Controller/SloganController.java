package com.polo.Blog.Controller;

import com.polo.Blog.Annootation.RequireAuth;
import com.polo.Blog.Domain.DTO.SloganDTO;
import com.polo.Blog.Domain.Entity.Slogan;
import com.polo.Blog.Service.SloganService;
import com.polo.Blog.Utils.Result;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/slogan")
public class SloganController {
    @Autowired
    private SloganService sloganService;

    @GetMapping("/list")
    public Result<List<Slogan>> getSloganList(){
        return sloganService.getSloganList();
    }

    @GetMapping("/deletedList")
    @RequireAuth
    public Result<IPage<Slogan>> getDeletedSloganList(@RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "20") int size){
        return sloganService.getDeletedSloganList(page, size);
    }

    @PostMapping("/publish")
    @RequireAuth
    public Result<String> publishSlogan(@RequestBody SloganDTO sloganDTO){
        return sloganService.publishSlogan(sloganDTO);
    }

    @PostMapping("/delete")
    @RequireAuth
    public Result<String> deleteSlogan(@RequestParam Long id){
        return sloganService.deleteSlogan(id);
    }

    @PostMapping("/restore")
    @RequireAuth
    public Result<String> restoreSlogan(@RequestParam Long id){
        return sloganService.restoreSlogan(id);
    }
}
