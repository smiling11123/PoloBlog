package com.polo.Blog.Controller;

import com.polo.Blog.Annootation.RequireAuth;
import com.polo.Blog.Domain.DTO.SloganDTO;
import com.polo.Blog.Domain.Entity.Slogan;
import com.polo.Blog.Service.SloganService;
import com.polo.Blog.Utils.Result;
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
}
