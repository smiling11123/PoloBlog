package com.polo.Blog.Controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.polo.Blog.Annootation.RequireAuth;
import com.polo.Blog.Domain.VO.CommentVO;
import com.polo.Blog.Service.CommentService;
import com.polo.Blog.Utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/comment")
public class CommentController {
    @Autowired
    private CommentService commentService;
    @RequireAuth
    @PostMapping("/publish")
    public Result<String> postComment(@RequestParam Long userId, @RequestParam String userName, @RequestParam Long articleId, @RequestParam (defaultValue = "-1") Long rootId, @RequestParam String content, @RequestParam (required = false) String toUserName, @RequestParam (required = false) Long toUserId){
        return commentService.publishComment(userId, userName, articleId, rootId, content, toUserName, toUserId);
    }

    @GetMapping("/rootComment")
    public Result<IPage<CommentVO>> getRootComment(@RequestParam Long articleId, @RequestParam (defaultValue = "1") Integer page, @RequestParam (defaultValue = "20") Integer size){
        return commentService.getRootComment(articleId, page, size);
    }

    @GetMapping("/childComment")
    public Result<IPage<CommentVO>> getChildComment(@RequestParam Long rootId, @RequestParam (defaultValue = "1") Integer page, @RequestParam (defaultValue = "20") Integer size){
        return commentService.getChildComment(rootId, page, size);
    }
}
