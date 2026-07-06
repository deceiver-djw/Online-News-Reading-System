package com.qst.onlinenewsbackend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.qst.onlinenewsbackend.common.Result;
import com.qst.onlinenewsbackend.entity.Category;
import com.qst.onlinenewsbackend.entity.Subscription;
import com.qst.onlinenewsbackend.service.CategoryService;
import com.qst.onlinenewsbackend.service.SubscriptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/subscription")
@Tag(name = "新闻订阅", description = "订阅、取消订阅、订阅设置相关接口")
public class SubscriptionController {

    @Autowired
    private SubscriptionService subscriptionService;

    @Autowired
    private CategoryService categoryService;

    @PostMapping
    @Operation(summary = "订阅分类", description = "用户订阅感兴趣的新闻分类")
    public Result<?> subscribe(@RequestParam Integer userId, @RequestParam Integer categoryId) {
        // 检查是否已订阅
        long count = subscriptionService.lambdaQuery()
                .eq(Subscription::getUserId, userId)
                .eq(Subscription::getCategoryId, categoryId)
                .count();
        if (count > 0) {
            return Result.error("已订阅该分类");
        }
        Subscription subscription = new Subscription();
        subscription.setUserId(userId);
        subscription.setCategoryId(categoryId);
        subscription.setIsPushEnabled(true);
        subscriptionService.save(subscription);

        // 填充分类信息
        subscription.setCategory(categoryService.getById(categoryId));

        return Result.success("订阅成功", subscription);
    }

    @DeleteMapping
    @Operation(summary = "取消订阅", description = "用户取消订阅指定分类")
    public Result<?> unsubscribe(@RequestParam Integer userId, @RequestParam Integer categoryId) {
        LambdaQueryChainWrapper<Subscription> wrapper = subscriptionService.lambdaQuery()
                .eq(Subscription::getUserId, userId)
                .eq(Subscription::getCategoryId, categoryId);
        subscriptionService.remove(wrapper);
        return Result.success("取消订阅成功");
    }

    @GetMapping("/my")
    @Operation(summary = "我的订阅列表", description = "获取用户已订阅的分类列表")
    public Result<?> getMySubscriptions(@RequestParam Integer userId) {
        List<Subscription> list = subscriptionService.lambdaQuery()
                .eq(Subscription::getUserId, userId)
                .list();

        // 批量填充分类名称和图标
        if (!list.isEmpty()) {
            Set<Integer> categoryIds = list.stream()
                    .map(Subscription::getCategoryId)
                    .collect(Collectors.toSet());

            Map<Integer, Category> categoryMap = categoryService.listByIds(categoryIds)
                    .stream()
                    .collect(Collectors.toMap(Category::getId, c -> c));

            for (Subscription sub : list) {
                sub.setCategory(categoryMap.get(sub.getCategoryId()));
            }
        }

        return Result.success(list);
    }

    @PutMapping("/{id}/push")
    @Operation(summary = "更新推送设置", description = "设置是否接收推送通知")
    public Result<?> updatePushSetting(@PathVariable Integer id, @RequestParam Boolean enabled) {
        Subscription subscription = subscriptionService.getById(id);
        if (subscription == null) {
            return Result.error("订阅不存在");
        }
        subscription.setIsPushEnabled(enabled);
        subscriptionService.updateById(subscription);
        return Result.success("设置成功", subscription);
    }

    @GetMapping("/check")
    @Operation(summary = "检查订阅状态", description = "检查用户是否已订阅指定分类")
    public Result<?> checkSubscription(@RequestParam Integer userId, @RequestParam Integer categoryId) {
        long count = subscriptionService.lambdaQuery()
                .eq(Subscription::getUserId, userId)
                .eq(Subscription::getCategoryId, categoryId)
                .count();
        return Result.success(count > 0);
    }
}
