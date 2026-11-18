package com.yunyou.shanhe.user.dto;

import jakarta.validation.constraints.NotNull;

public record FavoriteRequest(
        @NotNull(message = "收藏类型不能为空") Integer targetType,
        @NotNull(message = "收藏对象不能为空") Long targetId
) {
}
