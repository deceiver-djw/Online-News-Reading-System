package com.qst.onlinenewsbackend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qst.onlinenewsbackend.entity.LikeRecord;
import com.qst.onlinenewsbackend.mapper.LikeRecordMapper;
import com.qst.onlinenewsbackend.service.LikeRecordService;
import org.springframework.stereotype.Service;

@Service
public class LikeRecordServiceImpl extends ServiceImpl<LikeRecordMapper, LikeRecord> implements LikeRecordService {
}
