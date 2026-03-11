package com.polo.Blog.Schedule;

import com.polo.Blog.Domain.Entity.ArticleDaily;
import com.polo.Blog.Service.ArticleDailyService;
import com.polo.Blog.Service.ArticleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;


@Component
public class ArticleSchedule {
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private ArticleService articleService;
    @Autowired
    private ArticleDailyService articleDailyService;
    /**
     * 单篇文章每日统计 cron = "0 5 0 * * ?"
     */
    @Scheduled(cron = "0 5 0 * * ?")
    public void dailySchedule(){
        LocalDate yesToday = LocalDate.now().minusDays(1);
        String data = yesToday.toString();

        String viewCountKey = "blog_article_daily_stats:" + data + ":view_count";
        Map<Object, Object> viewMap = redisTemplate.opsForHash().entries(viewCountKey);

        for(Map.Entry<Object, Object> entry : viewMap.entrySet()){
            Long articleId = Long.parseLong((String) entry.getKey());
            Integer viewCount = Integer.parseInt((String) entry.getValue());
            ArticleDaily articleDaily = new ArticleDaily();

            articleDaily.setArticleId(articleId);
            articleDaily.setDate(yesToday);
            articleDaily.setViewCount(viewCount);
            articleDaily.setCreateTime(LocalDateTime.now());
            //点赞评论

            articleDailyService.save(articleDaily);
        }
        redisTemplate.delete(viewCountKey);
    }

    /**
     * 单篇文章每10min更新
     */
    @Scheduled(cron = "0 0/10 * * * ?")
    public void minSchedule(){
        String viewCountKey = "blog_article_view_count";
        Map<Object, Object> viewMap = redisTemplate.opsForHash().entries(viewCountKey);

        for(Map.Entry<Object, Object> entry : viewMap.entrySet()){
            Long articleId = Long.parseLong((String) entry.getKey());
            Integer viewCount = Integer.parseInt((String) entry.getValue());

            articleService.updateViewCountDaily(articleId, viewCount);

        }
        redisTemplate.delete(viewCountKey);
    }
}
