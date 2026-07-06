package com.qst.onlinenewsbackend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qst.onlinenewsbackend.entity.News;
import com.qst.onlinenewsbackend.entity.Tag;
import com.qst.onlinenewsbackend.mapper.TagMapper;
import com.qst.onlinenewsbackend.service.TagService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TagServiceImpl extends ServiceImpl<TagMapper, Tag> implements TagService {

    @Override
    public List<Tag> findTagsByNewsId(Integer newsId) {
        return ((TagMapper) baseMapper).findTagsByNewsId(newsId);
    }

    @Override
    public void populateTags(List<News> newsList) {
        if (newsList == null || newsList.isEmpty()) return;

        List<Integer> newsIds = newsList.stream()
                .map(News::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        if (newsIds.isEmpty()) return;

        // 批量查询所有标签
        List<Map<String, Object>> tagMaps = ((TagMapper) baseMapper).findTagMapsByNewsIds(newsIds);

        // 按 news_id 分组
        Map<Integer, List<Tag>> tagsByNewsId = new HashMap<>();
        for (Map<String, Object> row : tagMaps) {
            Integer newsId = (Integer) row.get("news_id");
            Tag tag = new Tag();
            tag.setId((Integer) row.get("id"));
            tag.setTagName((String) row.get("tag_name"));
            Object createdAt = row.get("created_at");
            if (createdAt instanceof LocalDateTime) {
                tag.setCreatedAt((LocalDateTime) createdAt);
            }
            tagsByNewsId.computeIfAbsent(newsId, k -> new ArrayList<>()).add(tag);
        }

        // 填充到每个 news
        for (News news : newsList) {
            news.setTags(tagsByNewsId.getOrDefault(news.getId(), Collections.emptyList()));
        }
    }
}
