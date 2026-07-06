package com.qst.onlinenewsbackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qst.onlinenewsbackend.entity.Tag;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface TagMapper extends BaseMapper<Tag> {

    /**
     * 根据新闻ID查询标签（通过 news_tags 中间表关联）
     */
    @Select("SELECT t.* FROM tags t INNER JOIN news_tags nt ON t.id = nt.tag_id WHERE nt.news_id = #{newsId}")
    List<Tag> findTagsByNewsId(@Param("newsId") Integer newsId);

    /**
     * 批量查询新闻的标签，返回 news_id 和 tag 的映射
     */
    @Select("<script>" +
            "SELECT nt.news_id, t.id, t.tag_name, t.created_at FROM tags t " +
            "INNER JOIN news_tags nt ON t.id = nt.tag_id " +
            "WHERE nt.news_id IN " +
            "<foreach collection='newsIds' item='id' open='(' separator=',' close=')'>#{id}</foreach>" +
            "</script>")
    List<Map<String, Object>> findTagMapsByNewsIds(@Param("newsIds") List<Integer> newsIds);

    /**
     * 插入新闻-标签关联（news_tags 中间表）
     */
    @Insert("INSERT INTO news_tags (news_id, tag_id) VALUES (#{newsId}, #{tagId})")
    void insertNewsTag(@Param("newsId") Integer newsId, @Param("tagId") Integer tagId);
}
