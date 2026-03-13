package com.polo.Blog.Service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.type.TypeReference;
import com.polo.Blog.Domain.Entity.MessageSlip;
import com.polo.Blog.Mapper.MessageSlipMapper;
import com.polo.Blog.Service.MessageSlipService;
import com.polo.Blog.Utils.RedisCache;
import com.polo.Blog.Utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class MessageSlipServiceImpl extends ServiceImpl<MessageSlipMapper, MessageSlip> implements MessageSlipService {
    @Autowired
    private RedisCache redisCache;
    @Autowired
    StringRedisTemplate stringRedisTemplate;
    @Override
    public Result<IPage<MessageSlip>> getMessageSlipList(Integer page, Integer size){
        LambdaQueryWrapper<MessageSlip> wrapper = new LambdaQueryWrapper<>();
        IPage<MessageSlip> pageInfo = new Page<>(page, size);

        wrapper.eq(MessageSlip::getIsDeleted, 0);

        return Result.success(this.page(pageInfo, wrapper));

    }

    @Override
    public Result<IPage<MessageSlip>> getDeletedMessageSlipList(Integer page, Integer size) {
        LambdaQueryWrapper<MessageSlip> wrapper = new LambdaQueryWrapper<>();
        IPage<MessageSlip> pageInfo = new Page<>(page, size);
        wrapper.eq(MessageSlip::getIsDeleted, 1).orderByDesc(MessageSlip::getCreateTime);
        return Result.success(this.page(pageInfo, wrapper));
    }

    @Override
    public Result<List<MessageSlip>> getMessageSlipToShow(Integer num){
        String key = "MessageSlipToShow";
        List<MessageSlip> messageSlipList = redisCache.getMessageToShowCache(key, new TypeReference<List<MessageSlip>>() {}, 50, this);
        if(messageSlipList != null) return Result.success(messageSlipList);

        return Result.success(new ArrayList<MessageSlip>());
    }

    @Override
    public Result<String> publishMessageSlip(String content, Long userId){
        if(content == null || content.trim().isEmpty()) {
            return Result.fail(400, "留言内容不能为空");
        }

        LocalDateTime now = LocalDateTime.now();
        MessageSlip messageSlip = new MessageSlip();
        messageSlip.setContent(content.trim());
        messageSlip.setCreateTime(now);
        messageSlip.setIsDeleted(0);

        if(userId == null) {
            messageSlip.setUserId(-1L);
        }else {
            messageSlip.setUserId(userId);
        }

        this.save(messageSlip);
        String date = LocalDate.now().toString();
        String newMessageSlipKey = "sys_daily_statistics:" + date + ":message_slip";
        stringRedisTemplate.opsForValue().increment(newMessageSlipKey);

        String key = "MessageSlipToShow";
        redisCache.deleteCache(key);
        redisCache.deleteCache("all_site_data");
        return Result.success("发布成功");
    }

    @Override
    public Result<String> deleteMessageSlip(Long id){
        MessageSlip messageSlip = this.getById(id);
        if (messageSlip == null || messageSlip.getIsDeleted() == 1) {
            return Result.fail(404, "留言不存在");
        }
        messageSlip.setIsDeleted(1);

        this.updateById(messageSlip);
        String key = "MessageSlipToShow";
        redisCache.deleteCache(key);
        redisCache.deleteCache("all_site_data");
        return Result.success("删除成功");
    }

    @Override
    public Result<String> restoreMessageSlip(Long id) {
        MessageSlip messageSlip = this.getById(id);
        if (messageSlip == null || messageSlip.getIsDeleted() == 0) {
            return Result.fail(404, "留言不存在");
        }
        messageSlip.setIsDeleted(0);
        this.updateById(messageSlip);
        redisCache.deleteCache("MessageSlipToShow");
        redisCache.deleteCache("all_site_data");
        return Result.success("恢复成功");
    }
}
