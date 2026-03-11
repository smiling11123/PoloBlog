package com.polo.Blog.Controller;

import com.polo.Blog.Domain.Entity.DailyStats;
import com.polo.Blog.Service.DailyStatsService;
import com.polo.Blog.Utils.Result;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/dailyStats")
public class DailyStatsController {
    @Autowired
    private DailyStatsService dailyStatsService;

    @GetMapping("/recent")
    public Result<List<DailyStats>> getRecentDailyStatsList(){
        return dailyStatsService.getRecentDailyStats();
    }

    @PostMapping("/addVisit")
    public Result<String> addVisitCount(HttpServletRequest httpServletRequest){
        return dailyStatsService.addVisitCount(httpServletRequest);
    }

}
