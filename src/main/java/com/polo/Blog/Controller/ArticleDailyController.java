package com.polo.Blog.Controller;

import com.polo.Blog.Domain.Entity.ArticleDaily;
import com.polo.Blog.Service.ArticleDailyService;
import com.polo.Blog.Utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/articleDaily")
public class ArticleDailyController {
    @Autowired
    private ArticleDailyService articleDailyService;

    @GetMapping("/recent")
    public Result<List<ArticleDaily>> getRecentArticleDaily(@RequestParam Long articleId){
        return articleDailyService.getRecentArticleDaily(articleId);
    }
}
