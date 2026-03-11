package com.polo.Blog.Schedule;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.polo.Blog.Domain.Entity.MessageSlip;
import com.polo.Blog.Service.MessageSlipService;
import com.polo.Blog.Utils.RedisCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
public class MessageSlipToShowSchedule {
    @Autowired
    private RedisCache redisCache;
    @Autowired
    private MessageSlipService messageSlipService;

    /**
     * 每 5 分钟重新装填弹幕缓存
     */
    @Scheduled(fixedRate = 5 * 60 * 1000)
    public void getMessageSlipToShowCache(){
        LambdaQueryWrapper<MessageSlip> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MessageSlip::getIsDeleted, 0).last("ORDER BY RAND() LIMIT 100");
        List<MessageSlip> messageSlipList = messageSlipService.list(wrapper);

        String key = "MessageSlipToShow";
        redisCache.deleteCache(key);
        redisCache.setMessageSet(key, messageSlipList, 5, TimeUnit.MINUTES);

    }
}
