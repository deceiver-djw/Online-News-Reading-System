package com.qst.onlinenewsbackend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qst.onlinenewsbackend.entity.StatisticSnapshot;
import com.qst.onlinenewsbackend.mapper.StatisticSnapshotMapper;
import com.qst.onlinenewsbackend.service.StatisticSnapshotService;
import org.springframework.stereotype.Service;

@Service
public class StatisticSnapshotServiceImpl extends ServiceImpl<StatisticSnapshotMapper, StatisticSnapshot> implements StatisticSnapshotService {
}
