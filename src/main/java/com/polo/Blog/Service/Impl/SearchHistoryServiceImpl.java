package com.polo.Blog.Service.Impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.polo.Blog.Domain.Entity.SearchHistory;
import com.polo.Blog.Mapper.SearchHistoryMapper;
import com.polo.Blog.Service.SearchHistoryService;
import org.springframework.stereotype.Service;

@Service
public class SearchHistoryServiceImpl extends ServiceImpl<SearchHistoryMapper, SearchHistory> implements SearchHistoryService {
}
