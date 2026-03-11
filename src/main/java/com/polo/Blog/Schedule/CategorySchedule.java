package com.polo.Blog.Schedule;

import com.polo.Blog.Service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Map;
@Component
public class CategorySchedule {
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private CategoryService categoryService;
    /**
     * 单分类每10min更新
     */
    @Scheduled(cron = "0 0/10 * * * ?")
    public void minSchedule(){
        String viewCountKey = "blog_category_view_count";
        Map<Object, Object> viewMap = redisTemplate.opsForHash().entries(viewCountKey);

        for(Map.Entry<Object, Object> entry : viewMap.entrySet()){
            Long categoryId = Long.parseLong((String) entry.getKey());
            Integer viewCount = Integer.parseInt((String) entry.getValue());

            categoryService.updateViewCount(categoryId, viewCount);
        }
        redisTemplate.delete(viewCountKey);
    }
}
