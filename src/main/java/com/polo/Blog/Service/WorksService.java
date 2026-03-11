package com.polo.Blog.Service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.polo.Blog.Domain.DTO.WorksDTO;
import com.polo.Blog.Domain.Entity.Works;
import com.polo.Blog.Domain.VO.WorksVO;
import com.polo.Blog.Utils.Result;

public interface WorksService extends IService<Works> {
    /**
     * 获取作品列表
     * @param page 页数
     * @param size 页大小
     * @return 返回一页
     */
    Result<IPage<WorksVO>> getWorksList(Integer page, Integer size);

    /**
     * 获取作品详情
     * @param id 作品Id
     * @return 然会数据
     */
    Result<Works> getWorksDetail(Long id);

    /**
     * 发布作品
     * @param worksDTO 作品
     * @return 返回处理结果
     */
    Result<String> publishWorks(WorksDTO worksDTO);

    /**
     * 修改作品
     * @param worksDTO 修改对象
     * @return 返回处理结果
     */
    Result<String> updateWorks(WorksDTO worksDTO);

    /**
     * 删除作品
     * @param id 作品Id
     * @return 返回处理结果
     */
    Result<String> deleteWorks(Long id);
}
