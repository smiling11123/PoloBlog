package com.polo.Blog.Service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.polo.Blog.Domain.Entity.AllSiteDta;
import com.polo.Blog.Utils.Result;

public interface AllSiteDataService extends IService<AllSiteDta> {
    /**
     * 获取全站数据
     * @return 返回全站数据
     */
    Result<AllSiteDta> getAllSiteData();
}
