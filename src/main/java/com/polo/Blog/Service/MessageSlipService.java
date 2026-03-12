package com.polo.Blog.Service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.polo.Blog.Domain.Entity.MessageSlip;
import com.polo.Blog.Utils.Result;

import java.util.List;

public interface MessageSlipService extends IService<MessageSlip> {

    /**
     * 分页获取所有弹幕留言 用于管理
     * @param page 页数
     * @param size 页大小
     * @return 返回一页
     */
    Result<IPage<MessageSlip>> getMessageSlipList(Integer page, Integer size);

    /**
     * 获取已删除留言
     * @param page 页数
     * @param size 页大小
     * @return 返回一页
     */
    Result<IPage<MessageSlip>> getDeletedMessageSlipList(Integer page, Integer size);

    /**
     * 获取 N 条弹幕用于首页展示
     * @param num 数量
     * @return 返回弹幕列表
     */
    Result<List<MessageSlip>> getMessageSlipToShow(Integer num);

    /**
     * 发布弹幕留言
     * @param content 内容
     * @param userId 用户id
     * @return 返回发布结果
     */
    Result<String> publishMessageSlip(String content, Long userId);

    /**
     * 删除弹幕留言
     * @param id 弹幕Id
     * @return 返回处理结果
     */
    Result<String> deleteMessageSlip(Long id);

    /**
     * 恢复留言
     * @param id 留言id
     * @return 返回处理结果
     */
    Result<String> restoreMessageSlip(Long id);
}
