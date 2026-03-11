package com.polo.Blog.Controller;

import com.polo.Blog.Annootation.RequireAuth;
import com.polo.Blog.Domain.DTO.CategoryDTO;
import com.polo.Blog.Domain.Entity.Category;
import com.polo.Blog.Service.CategoryService;
import com.polo.Blog.Utils.Result;
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
}
