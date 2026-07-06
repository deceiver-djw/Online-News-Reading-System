package com.qst.onlinenewsbackend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qst.onlinenewsbackend.entity.News;
import com.qst.onlinenewsbackend.mapper.NewsMapper;
import com.qst.onlinenewsbackend.service.NewsService;
import org.springframework.stereotype.Service;

@Service
public class NewsServiceImpl extends ServiceImpl<NewsMapper, News> implements NewsService {
}
