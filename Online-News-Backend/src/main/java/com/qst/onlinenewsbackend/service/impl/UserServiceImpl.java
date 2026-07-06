package com.qst.onlinenewsbackend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qst.onlinenewsbackend.entity.User;
import com.qst.onlinenewsbackend.mapper.UserMapper;
import com.qst.onlinenewsbackend.service.UserService;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
}
