package com.polo.Blog.Service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.polo.Blog.Domain.Entity.DailyStats;
import com.polo.Blog.Utils.Result;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

public interface DailyStatsService extends IService<DailyStats> {
    /**
     * 获取最近 N 天的站点数据
     * @return 返回最近 N 天的站点数据
     */
    Result<List<DailyStats>> getRecentDailyStats();

    /**
     * 增加访问量、访客
     * @param httpServletRequest 请求参数
     * @return 放回增加成功
     */
    Result<String> addVisitCount(HttpServletRequest httpServletRequest);
}
