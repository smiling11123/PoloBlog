package com.polo.Blog.Service.Impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.polo.Blog.Domain.Entity.AllSiteDta;
import com.polo.Blog.Mapper.AllSiteDtaMapper;
import com.polo.Blog.Service.AllSiteDataService;
import com.polo.Blog.Utils.RedisCache;
import com.polo.Blog.Utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class AllSiteDataServiceImpl extends ServiceImpl<AllSiteDtaMapper, AllSiteDta> implements AllSiteDataService {
    @Autowired
    private RedisCache redisCache;

    @Override
    public Result<AllSiteDta> getAllSiteData(){
        String key = "all_site_data";
        AllSiteDta allSiteDtaCache = redisCache.get(key, AllSiteDta.class);
        if(allSiteDtaCache != null) return Result.success(allSiteDtaCache);

        AllSiteDta allSiteDta = getOrCreateAllSiteData();

        redisCache.set(key, allSiteDta, 24, TimeUnit.HOURS);

        return Result.success(allSiteDta);

    }

    private AllSiteDta getOrCreateAllSiteData() {
        AllSiteDta allSiteDta = this.getById(1L);
        if (allSiteDta != null) {
            return allSiteDta;
        }

        AllSiteDta defaultAllSiteData = new AllSiteDta();
        defaultAllSiteData.setId(1L);
        defaultAllSiteData.setTotalUserCount(0);
        defaultAllSiteData.setTotalArticleCount(0);
        defaultAllSiteData.setTotalCommentCount(0);
        defaultAllSiteData.setTotalViewCount(0L);
        defaultAllSiteData.setTotalVisitCount(0L);
        defaultAllSiteData.setTotalVisitorCount(0L);
        defaultAllSiteData.setTotalMessageSlipCount(0L);
        defaultAllSiteData.setUpdateTime(java.time.LocalDateTime.now());
        this.save(defaultAllSiteData);
        return defaultAllSiteData;
    }
}
