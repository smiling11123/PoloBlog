package com.polo.Blog.Controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.polo.Blog.Annootation.RequireAuth;
import com.polo.Blog.Domain.DTO.ArticleDTO;
import com.polo.Blog.Domain.VO.ArticleVO;
import com.polo.Blog.Service.ArticleService;
import com.polo.Blog.Utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/article")
public class ArticleController {
    @Autowired
    private ArticleService articleService;

    //根据文章id获取文章详细内容
    @GetMapping("/detail")
    public Result<ArticleVO> getArticleById(@RequestParam Long id) {
        return articleService.getArticleById(id);
    }

    //获取文章列表
    @GetMapping("/list")
    public Result<IPage<ArticleVO>> getArticleList(@RequestParam (defaultValue = "1") int page, @RequestParam (defaultValue = "20") int size) {
        return articleService.getArticleList(page, size);
    }
    //根据标签获取文章
    @GetMapping("/listByTag")
    public Result<IPage<ArticleVO>> getArticleListByTags(@RequestParam List<String> tagId, @RequestParam (defaultValue = "1") int page, @RequestParam (defaultValue = "20") int size){
        return articleService.getArticleByTag(tagId, page, size);
    }
    //根据分类获取文章
    @GetMapping("/listByCategory")
    public Result<IPage<ArticleVO>> getArticleListByCategory(@RequestParam Long categoryId, @RequestParam (defaultValue = "1") int page, @RequestParam (defaultValue = "20") int size){
        return articleService.getArticleByCategory(categoryId, page, size);
    }
    //搜索文章
    @GetMapping("/search")
    public Result<IPage<ArticleVO>> getArticleByKeyWord(@RequestParam (defaultValue = "") String keyword, @RequestParam (defaultValue = "") List<Long> categoryId, @RequestParam (defaultValue = "1") int page, @RequestParam (defaultValue = "20") int size){
        return articleService.getArticleByKeyWord(keyword, categoryId, page, size);
    }
    //获取热门文章
    @GetMapping("/hotList")
    public Result<IPage<ArticleVO>> getHotArticleByViewCount(@RequestParam (defaultValue = "1") int page, @RequestParam (defaultValue = "5") int size){
        return articleService.getHotArticle(page, size);
    }
    //======================================管理员权限==========================================//
    @RequireAuth
    @GetMapping("/deletedList")
    public Result<IPage<ArticleVO>> getDeletedArticleList(@RequestParam (defaultValue = "1") int page, @RequestParam (defaultValue = "6") int size){
        return articleService.getDeletedArticleList(page, size);
    }

    @RequireAuth
    @GetMapping("/manuscript")
    public Result<IPage<ArticleVO>> getManuscriptList(@RequestParam (defaultValue = "1") int page, @RequestParam (defaultValue = "20") int size){
        return articleService.getManuscriptList(page, size);
    }
    @RequireAuth
    @PostMapping("/publish")
    public Result<String> publishArticle(@RequestBody ArticleDTO articleDTO) {
        return articleService.publishArticle(articleDTO);
    }
    @RequireAuth
    @PostMapping("/update")
    public Result<String> updateArticle(@RequestBody ArticleDTO articleDTO) {
        return articleService.updateArticle(articleDTO);
    }
    @RequireAuth
    @PostMapping("/delete")
    public Result<String> deleteArticle(@RequestParam Long id){
        return articleService.deleteArticle(id);
    }
    @RequireAuth
    @PostMapping("/manuscriptPublish")
    public Result<String> publishManuscript(@RequestBody ArticleDTO articleDTO) {
        return articleService.manuscriptToArticle(articleDTO);
    }

    @RequireAuth
    @PostMapping("/toggleComment")
    public Result<String> toggleCommentStatus(@RequestParam Long id) {
        return articleService.toggleCommentStatus(id);
    }

}
