package com.qst.onlinenewsbackend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qst.onlinenewsbackend.entity.Notification;
import com.qst.onlinenewsbackend.mapper.NotificationMapper;
import com.qst.onlinenewsbackend.service.NotificationService;
import org.springframework.stereotype.Service;

@Service
public class NotificationServiceImpl extends ServiceImpl<NotificationMapper, Notification> implements NotificationService {
}
