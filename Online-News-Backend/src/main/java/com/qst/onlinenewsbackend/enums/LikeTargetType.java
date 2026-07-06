package com.qst.onlinenewsbackend.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;

public enum LikeTargetType {
    NEWS("news"),
    COMMENT("comment");

    @EnumValue
    private final String value;

    LikeTargetType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    /**
     * 根据前端传入的字符串值查找枚举（不区分大小写）
     */
    public static LikeTargetType fromValue(String val) {
        for (LikeTargetType type : values()) {
            if (type.value.equalsIgnoreCase(val) || type.name().equalsIgnoreCase(val)) {
                return type;
            }
        }
        throw new IllegalArgumentException("无效的点赞目标类型: " + val);
    }
}
