package com.qst.onlinenewsbackend.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;

public enum CommentStatus {
    NORMAL("normal"),
    DELETED("deleted");

    @EnumValue
    private final String value;

    CommentStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
