package com.qst.onlinenewsbackend.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;

public enum NewsStatus {
    DRAFT("draft"),
    PENDING("pending"),
    PUBLISHED("published"),
    ARCHIVED("archived"),
    REJECTED("rejected");

    @EnumValue
    private final String value;

    NewsStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
