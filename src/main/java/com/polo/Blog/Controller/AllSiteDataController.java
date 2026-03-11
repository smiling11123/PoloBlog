package com.polo.Blog.Controller;

import com.polo.Blog.Domain.Entity.AllSiteDta;
import com.polo.Blog.Service.AllSiteDataService;
import com.polo.Blog.Utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/allSiteData")
public class AllSiteDataController {

    @Autowired
    private AllSiteDataService allSiteDataService;

    @GetMapping("/get")
    public Result<AllSiteDta> getAllSiteData(){
        return allSiteDataService.getAllSiteData();
    }
}
