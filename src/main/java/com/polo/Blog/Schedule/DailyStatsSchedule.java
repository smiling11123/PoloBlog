package com.polo.Blog.Schedule;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.polo.Blog.Domain.Entity.AllSiteDta;
import com.polo.Blog.Domain.Entity.DailyStats;
import com.polo.Blog.Service.AllSiteDataService;
import com.polo.Blog.Service.DailyStatsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 每日统计表 sys_daily_statistics
 */
@Component
public class DailyStatsSchedule {
    @Autowired
    private DailyStatsService dailyStatsService;
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private AllSiteDataService allSiteDataService;
    // 每天凌晨 0点05分 执行
    @Scheduled(cron = "0 5 0 * * ?")
    public void dailyStatsSchedule() {
        LocalDate yesToday = LocalDate.now().minusDays(1);
        String date = yesToday.toString();

        //拼接key
        String newLoginUser = "sys_daily_statistics:" + date + ":new_login_user"; //注册
        String newViewCount = "sys_daily_statistics:" + date + ":view_count";
        String newArticleCount = "sys_daily_statistics:" + date + ":new_article_count";
        String newCommentCount = "sys_daily_statistics:" + date + ":new_comment_count";
        String totalVisitCountKey = "sys_daily_statistics:" + date + ":visit_count";
        String totalVisitorCountKey = "sys_daily_statistics:" + date + ":visitor_count";
        String newMessageSlipCountKey = "sys_daily_statistics:" + date + ":message_slip";

        String loginUserCount = redisTemplate.opsForValue().get(newLoginUser);
        String viewCount = redisTemplate.opsForValue().get(newViewCount);
        String articleCount = redisTemplate.opsForValue().get(newArticleCount);
        String newComment = redisTemplate.opsForValue().get(newCommentCount);
        String totalVisitCount = redisTemplate.opsForValue().get(totalVisitCountKey);
        Long totalVisitorCounts = redisTemplate.opsForHyperLogLog().size(totalVisitorCountKey);
        String newMessageSlipCount = redisTemplate.opsForValue().get(newMessageSlipCountKey);

        int loginUserCounts = loginUserCount == null ? 0 : Integer.parseInt(loginUserCount);
        Long viewCounts = viewCount == null ? 0 : Long.parseLong(viewCount);
        int articleCounts = articleCount == null ? 0 : Integer.parseInt(articleCount);
        int newComments = newComment == null ? 0 : Integer.parseInt(newComment);
        Long totalVisitCounts = totalVisitCount == null ? 0 : Long.parseLong(totalVisitCount);
        Long newMessageSlipCounts = newMessageSlipCount == null ? 0 : Long.parseLong(newMessageSlipCount);

        DailyStats dailyStats = new DailyStats();
        dailyStats.setDate(yesToday);
        dailyStats.setCreateTime(LocalDateTime.now());
        dailyStats.setLoginUserCount(loginUserCounts);
        dailyStats.setTotalViewCount(viewCounts);
        dailyStats.setNewArticleCount(articleCounts);
        dailyStats.setNewCommentCount(newComments);
        dailyStats.setTotalVisitCount(totalVisitCounts);
        dailyStats.setTotalVisitorCount(totalVisitorCounts);
        dailyStats.setMessageSlipCount(newMessageSlipCounts);

        allSiteDataService.update(new UpdateWrapper<AllSiteDta>().eq("id","1")
                .setSql("total_user_count = total_user_count + " + loginUserCounts)
                .setSql("total_article_count = total_article_count + " + articleCounts)
                .setSql("total_comment_count = total_comment_count + " + newComments)
                .setSql("total_view_count = total_view_count + " + viewCounts)
                .setSql("total_visit_count = total_visit_count + " + totalVisitCounts)
                .setSql("total_visitor_count = total_visitor_count + " + totalVisitorCounts)
                .setSql("update_time = '" + date + "'")
                .setSql("total_message_slip_count = total_message_slip_count + " + newMessageSlipCounts));

        String allSiteDataCacheKey = "all_site_data";
        redisTemplate.delete(allSiteDataCacheKey);

        try {
            //存入数据库
            dailyStatsService.save(dailyStats);
            //删除key
            redisTemplate.delete(newLoginUser);
            redisTemplate.delete(newViewCount);
            redisTemplate.delete(newArticleCount);
            redisTemplate.delete("DailyStats");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


}
