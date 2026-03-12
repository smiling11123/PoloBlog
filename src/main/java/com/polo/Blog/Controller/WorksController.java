package com.polo.Blog.Controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.polo.Blog.Annootation.RequireAuth;
import com.polo.Blog.Domain.DTO.WorksDTO;
import com.polo.Blog.Domain.Entity.Works;
import com.polo.Blog.Domain.VO.WorksVO;
import com.polo.Blog.Service.WorksService;
import com.polo.Blog.Utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/works")
public class WorksController {
    @Autowired
    private WorksService worksService;

    @GetMapping("/list")
    public Result<IPage<WorksVO>> getWorksList(@RequestParam (defaultValue = "1") Integer page, @RequestParam (defaultValue = "5") Integer size){
        return worksService.getWorksList(page, size);
    }

    @RequireAuth
    @GetMapping("/deletedList")
    public Result<IPage<WorksVO>> getDeletedWorksList(@RequestParam (defaultValue = "1") Integer page, @RequestParam (defaultValue = "10") Integer size){
        return worksService.getDeletedWorksList(page, size);
    }

    @GetMapping("/detail")
    public Result<Works> getWorksDetail(@RequestParam Long id){
        return worksService.getWorksDetail(id);
    }

    @RequireAuth
    @PostMapping("/update")
    public Result<String> updateWorks(@RequestBody WorksDTO worksDTO){
        return worksService.updateWorks(worksDTO);
    }

    @RequireAuth
    @PostMapping("/publish")
    public Result<String> publishWorks(@RequestBody WorksDTO worksDTO){
        return worksService.publishWorks(worksDTO);
    }
    @RequireAuth
    @PostMapping("/delete")
    public Result<String> deleteWorks(@RequestParam Long id){
        return worksService.deleteWorks(id);
    }

    @RequireAuth
    @PostMapping("/restore")
    public Result<String> restoreWorks(@RequestParam Long id){
        return worksService.restoreWorks(id);
    }
}
