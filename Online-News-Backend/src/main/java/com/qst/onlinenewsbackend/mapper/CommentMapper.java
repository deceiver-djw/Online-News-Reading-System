package com.qst.onlinenewsbackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qst.onlinenewsbackend.entity.Comment;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CommentMapper extends BaseMapper<Comment> {
}
