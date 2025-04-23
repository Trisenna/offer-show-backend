package com.offershow.util;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 统一响应结果类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResponseResult<T> {
    /**
     * 状态码
     */
    private int code;

    /**
     * 消息
     */
    private String message;

    /**
     * 数据
     */
    private T data;

    /**
     * 是否成功
     */
    private boolean success;

    /**
     * 创建成功响应
     *
     * @param data 响应数据
     * @param <T>  数据类型
     * @return 响应结果
     */
    public static <T> ResponseResult<T> success(T data) {
        return ResponseResult.<T>builder()
                .code(200)
                .message("success")
                .data(data)
                .success(true)
                .build();
    }

    /**
     * 创建成功响应 (无数据)
     *
     * @return 响应结果
     */
    public static <T> ResponseResult<T> success() {
        return success(null);
    }

    /**
     * 创建失败响应
     *
     * @param code    错误码
     * @param message 错误消息
     * @param <T>     数据类型
     * @return 响应结果
     */
    public static <T> ResponseResult<T> error(int code, String message) {
        return ResponseResult.<T>builder()
                .code(code)
                .message(message)
                .data(null)
                .success(false)
                .build();
    }

    /**
     * 创建参数错误响应
     *
     * @param message 错误消息
     * @param <T>     数据类型
     * @return 响应结果
     */
    public static <T> ResponseResult<T> paramError(String message) {
        return error(400, message);
    }

    /**
     * 创建资源不存在响应
     *
     * @param message 错误消息
     * @param <T>     数据类型
     * @return 响应结果
     */
    public static <T> ResponseResult<T> notFound(String message) {
        return error(404, message);
    }

    /**
     * 创建服务器内部错误响应
     *
     * @param message 错误消息
     * @param <T>     数据类型
     * @return 响应结果
     */
    public static <T> ResponseResult<T> serverError(String message) {
        return error(500, message);
    }
}