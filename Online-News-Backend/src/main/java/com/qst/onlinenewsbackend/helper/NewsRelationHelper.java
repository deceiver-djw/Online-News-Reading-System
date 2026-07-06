package com.qst.onlinenewsbackend.helper;

import com.qst.onlinenewsbackend.entity.Category;
import com.qst.onlinenewsbackend.entity.News;
import com.qst.onlinenewsbackend.entity.Tag;
import com.qst.onlinenewsbackend.mapper.TagMapper;
import com.qst.onlinenewsbackend.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 新闻关联数据填充助手
 * MyBatis-Plus 不会自动填充 @TableField(exist = false) 的关联字段，
 * 需要在查询后手动填充 category 和 tags 字段。
 */
@Component
public class NewsRelationHelper {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private TagMapper tagMapper;

    /**
     * 为新闻列表批量填充 category 和 tags
     */
    public void populateAll(List<News> newsList) {
        if (newsList == null || newsList.isEmpty()) return;
        populateCategories(newsList);
        populateTags(newsList);
    }

    /**
     * 为单个新闻填充 category 和 tags
     */
    public void populateAll(News news) {
        if (news == null) return;
        if (news.getCategoryId() != null) {
            news.setCategory(categoryService.getById(news.getCategoryId()));
        }
        news.setTags(tagMapper.findTagsByNewsId(news.getId()));
    }

    /**
     * 批量填充分类
     */
    private void populateCategories(List<News> newsList) {
        Set<Integer> categoryIds = newsList.stream()
                .map(News::getCategoryId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        if (categoryIds.isEmpty()) return;

        Map<Integer, Category> categoryMap = categoryService.listByIds(categoryIds)
                .stream()
                .collect(Collectors.toMap(Category::getId, c -> c));

        for (News news : newsList) {
            if (news.getCategoryId() != null) {
                news.setCategory(categoryMap.get(news.getCategoryId()));
            }
        }
    }

    /**
     * 批量填充标签
     */
    private void populateTags(List<News> newsList) {
        List<Integer> newsIds = newsList.stream()
                .map(News::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        if (newsIds.isEmpty()) return;

        List<Map<String, Object>> tagMaps = tagMapper.findTagMapsByNewsIds(newsIds);

        Map<Integer, List<Tag>> tagsByNewsId = new HashMap<>();
        for (Map<String, Object> row : tagMaps) {
            Integer newsId = (Integer) row.get("news_id");
            Tag tag = new Tag();
            tag.setId((Integer) row.get("id"));
            tag.setTagName((String) row.get("tag_name"));
            tagsByNewsId.computeIfAbsent(newsId, k -> new ArrayList<>()).add(tag);
        }

        for (News news : newsList) {
            news.setTags(tagsByNewsId.getOrDefault(news.getId(), Collections.emptyList()));
        }
    }
}
