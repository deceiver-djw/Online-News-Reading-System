package com.qst.onlinenewsbackend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qst.onlinenewsbackend.entity.Subscription;
import com.qst.onlinenewsbackend.mapper.SubscriptionMapper;
import com.qst.onlinenewsbackend.service.SubscriptionService;
import org.springframework.stereotype.Service;

@Service
public class SubscriptionServiceImpl extends ServiceImpl<SubscriptionMapper, Subscription> implements SubscriptionService {
}
