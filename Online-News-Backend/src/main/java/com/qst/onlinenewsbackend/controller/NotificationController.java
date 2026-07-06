package com.qst.onlinenewsbackend.controller;

import com.qst.onlinenewsbackend.common.Result;
import com.qst.onlinenewsbackend.entity.Notification;
import com.qst.onlinenewsbackend.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 通知控制器
 * 模块9：新闻订阅与推送设置模块
 */
@RestController
@RequestMapping("/api/notification")
@Tag(name = "系统通知", description = "通知列表、标记已读、未读数量相关接口")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @GetMapping("/my")
    @Operation(summary = "我的通知列表", description = "获取用户的通知列表（按时间倒序）")
    public Result<?> getMyNotifications(@RequestParam Integer userId) {
        List<Notification> list = notificationService.lambdaQuery()
                .eq(Notification::getUserId, userId)
                .orderByDesc(Notification::getCreateTime)
                .list();
        return Result.success(list);
    }

    @PutMapping("/{id}/read")
    @Operation(summary = "标记已读", description = "标记指定通知为已读状态")
    public Result<?> markAsRead(@PathVariable Integer id) {
        Notification notification = notificationService.getById(id);
        if (notification == null) {
            return Result.error("通知不存在");
        }
        notification.setIsRead(true);
        notificationService.updateById(notification);
        return Result.success("标记成功", notification);
    }

    @PutMapping("/read-all")
    @Operation(summary = "全部标记已读", description = "将用户所有未读通知标记为已读")
    public Result<?> markAllAsRead(@RequestParam Integer userId) {
        notificationService.lambdaQuery()
                .eq(Notification::getUserId, userId)
                .eq(Notification::getIsRead, false)
                .list()
                .forEach(n -> {
                    n.setIsRead(true);
                    notificationService.updateById(n);
                });
        return Result.success("全部标记已读");
    }

    @GetMapping("/unread-count")
    @Operation(summary = "未读通知数量", description = "获取用户未读通知数量（用于红点提醒）")
    public Result<?> getUnreadCount(@RequestParam Integer userId) {
        long count = notificationService.lambdaQuery()
                .eq(Notification::getUserId, userId)
                .eq(Notification::getIsRead, false)
                .count();
        return Result.success(count);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除通知", description = "删除指定通知")
    public Result<?> deleteNotification(@PathVariable Integer id) {
        notificationService.removeById(id);
        return Result.success("删除成功");
    }
}
