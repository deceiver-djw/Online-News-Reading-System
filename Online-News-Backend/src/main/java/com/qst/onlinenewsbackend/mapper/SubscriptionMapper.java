package com.qst.onlinenewsbackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qst.onlinenewsbackend.entity.Subscription;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SubscriptionMapper extends BaseMapper<Subscription> {
}
