package com.polo.Blog.Service.Impl;

import cn.hutool.http.server.HttpServerRequest;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.type.TypeReference;
import com.polo.Blog.Domain.Entity.DailyStats;
import com.polo.Blog.Mapper.DailyStatsMapper;
import com.polo.Blog.Service.DailyStatsService;
import com.polo.Blog.Utils.IpUtils;
import com.polo.Blog.Utils.RedisCache;
import com.polo.Blog.Utils.Result;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class DailyStatsServiceImpl extends ServiceImpl<DailyStatsMapper, DailyStats> implements DailyStatsService {
    @Autowired
    private RedisCache redisCache;
    @Autowired
    StringRedisTemplate redisTemplate;
    @Override
    public Result<List<DailyStats>> getRecentDailyStats(){
        String key = "DailyStats";
        List<DailyStats> dailyStatsCache = redisCache.get(key, new TypeReference<List<DailyStats>>() {});
        if(dailyStatsCache != null) return Result.success(dailyStatsCache);
        LambdaQueryWrapper<DailyStats> wrapper = new LambdaQueryWrapper<>();
        wrapper.last("LIMIT 30");
        List<DailyStats> dailyStats = this.list();

        redisCache.set(key, dailyStats, 23, TimeUnit.HOURS);
        return Result.success(dailyStats);
    }
    @Override
    public Result<String> addVisitCount(HttpServletRequest httpServletRequest){
        String ip = IpUtils.getClientIp(httpServletRequest);
        String today = LocalDate.now().toString();
        String key = "sys_daily_statistics:" + today + ":visit_count";
        redisTemplate.opsForValue().increment(key);

        String visitorKey = "sys_daily_statistics:" + today + ":visitor_count";
        redisTemplate.opsForHyperLogLog().add(visitorKey, ip);

        return Result.success("增加成功");
    }
}
