package com.qst.onlinenewsbackend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qst.onlinenewsbackend.entity.ReadHistory;
import com.qst.onlinenewsbackend.mapper.ReadHistoryMapper;
import com.qst.onlinenewsbackend.service.ReadHistoryService;
import org.springframework.stereotype.Service;

@Service
public class ReadHistoryServiceImpl extends ServiceImpl<ReadHistoryMapper, ReadHistory> implements ReadHistoryService {
}
