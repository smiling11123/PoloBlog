package com.polo.Blog.Controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.polo.Blog.Annootation.RequireAuth;
import com.polo.Blog.Domain.Entity.MessageSlip;
import com.polo.Blog.Service.MessageSlipService;
import com.polo.Blog.Utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/messageSlip")
public class MessageSlipController {
    @Autowired
    private MessageSlipService messageSlipService;

    @GetMapping("/list")
    public Result<IPage<MessageSlip>> getMessageSlipList(@RequestParam (defaultValue = "1") Integer page, @RequestParam (defaultValue = "20") Integer size){
        return messageSlipService.getMessageSlipList(page, size);
    }

    @RequireAuth
    @GetMapping("/deletedList")
    public Result<IPage<MessageSlip>> getDeletedMessageSlipList(@RequestParam (defaultValue = "1") Integer page, @RequestParam (defaultValue = "20") Integer size){
        return messageSlipService.getDeletedMessageSlipList(page, size);
    }

    @GetMapping("/showList")
    public Result<List<MessageSlip>> getMessageSlipToShow(@RequestParam (defaultValue = "20") Integer num){
        return messageSlipService.getMessageSlipToShow(num);
    }
    @PostMapping("/publish")
    public Result<String> publishMessageSlip(@RequestParam String content, @RequestParam (required = false) Long userId){
        return messageSlipService.publishMessageSlip(content, userId);
    }
    @RequireAuth
    @PostMapping("/delete")
    public Result<String> deleteMessageSlip(@RequestParam Long id){
        return messageSlipService.deleteMessageSlip(id);
    }

    @RequireAuth
    @PostMapping("/restore")
    public Result<String> restoreMessageSlip(@RequestParam Long id){
        return messageSlipService.restoreMessageSlip(id);
    }
}
