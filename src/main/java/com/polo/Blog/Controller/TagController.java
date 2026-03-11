package com.polo.Blog.Controller;

import com.polo.Blog.Annootation.RequireAuth;
import com.polo.Blog.Domain.Entity.Tag;
import com.polo.Blog.Service.TagService;
import com.polo.Blog.Utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tag")
public class TagController {
    @Autowired
    private TagService tagService;

    @GetMapping("/list")
    public Result<List<Tag>> getTags(){
        return tagService.getTags();
    }

    @RequireAuth
    @PostMapping("/create")
    public Result<String> createTag(@RequestParam String name){
        return tagService.createTag(name);
    }

    @RequireAuth
    @PostMapping("/update")
    public Result<String> updateTag(@RequestParam (defaultValue = "") String name, @RequestParam (defaultValue = "0") Integer isDelete, @RequestParam Long id){
        return tagService.updateTag(name, isDelete, id);
    }
}
