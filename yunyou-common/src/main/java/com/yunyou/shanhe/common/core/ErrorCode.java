package com.yunyou.shanhe.common.core;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 统一错误码。
 */
@Getter
@AllArgsConstructor
public enum ErrorCode {

    SUCCESS(0, "成功"),
    PARAM_ERROR(400, "参数错误"),
    UNAUTHORIZED(401, "未登录或登录已过期"),
    FORBIDDEN(403, "无权限"),
    NOT_FOUND(404, "资源不存在"),
    TOO_MANY_REQUESTS(429, "请求过于频繁"),
    SYSTEM_ERROR(500, "系统繁忙，请稍后再试"),
    BIZ_ERROR(1000, "业务处理失败");

    private final int code;
    private final String message;
}
