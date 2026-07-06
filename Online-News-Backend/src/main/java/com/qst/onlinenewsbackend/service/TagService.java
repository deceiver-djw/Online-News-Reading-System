package com.qst.onlinenewsbackend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.qst.onlinenewsbackend.entity.News;
import com.qst.onlinenewsbackend.entity.Tag;

import java.util.List;

public interface TagService extends IService<Tag> {

    /**
     * 根据新闻ID查询标签
     */
    List<Tag> findTagsByNewsId(Integer newsId);

    /**
     * 为新闻列表批量填充标签
     */
    void populateTags(List<News> newsList);
}
