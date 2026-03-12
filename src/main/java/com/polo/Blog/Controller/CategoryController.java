package com.polo.Blog.Controller;

import com.polo.Blog.Annootation.RequireAuth;
import com.polo.Blog.Domain.DTO.CategoryDTO;
import com.polo.Blog.Domain.Entity.Category;
import com.polo.Blog.Service.CategoryService;
import com.polo.Blog.Utils.Result;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/category")
public class CategoryController {
    @Autowired
    private CategoryService categoryService;

    @GetMapping("/hotList")
    public Result<List<Category>> getHotCategoryList(){
        return categoryService.getHotCategoryList();
    }

    @GetMapping("/list")
    public Result<List<Category>> getCategoryList(){
        return categoryService.getCategoryList();
    }

    @RequireAuth
    @GetMapping("/deletedList")
    public Result<IPage<Category>> getDeletedCategoryList(@RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "20") int size){
        return categoryService.getDeletedCategoryList(page, size);
    }

    @PostMapping("/create")
    @RequireAuth
    public Result createCategory(@RequestBody CategoryDTO categoryDTO){
        return categoryService.createCategory(categoryDTO);
    }

    @PostMapping("/delete")
    @RequireAuth
    public Result deleteCategory(@RequestParam Long id){
        return categoryService.deleteCategory(id);
    }

    @PostMapping("/restore")
    @RequireAuth
    public Result restoreCategory(@RequestParam Long id){
        return categoryService.restoreCategory(id);
    }
}
