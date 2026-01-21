package com.example.planningpoker.presentation.dto.response;

/**
 * 统一响应包装类
 * 
 * <p>所有HTTP响应都使用此类进行包装，提供统一的响应格式。
 * 
 * <p>响应格式：
 * <pre>
 * {
 *   "code": 200,
 *   "message": "成功",
 *   "data": { ... }
 * }
 * </pre>
 * 
 * @param <T> 响应数据类型
 * @author 开发团队
 * @since 1.0.0
 * @date 2026-01-18
 */
public class ResponseDTO<T> {
    
    /**
     * HTTP状态码
     */
    private Integer code;
    
    /**
     * 响应消息
     */
    private String message;
    
    /**
     * 响应数据
     */
    private T data;
    
    public ResponseDTO() {
    }
    
    public ResponseDTO(Integer code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }
    
    public Integer getCode() { return code; }
    public void setCode(Integer code) { this.code = code; }
    
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    
    public T getData() { return data; }
    public void setData(T data) { this.data = data; }
    
    /**
     * 成功响应
     * 
     * @param data 响应数据
     * @param <T> 数据类型
     * @return 响应对象
     */
    public static <T> ResponseDTO<T> success(T data) {
        return new ResponseDTO<T>(200, "成功", data);
    }
    
    /**
     * 成功响应（带自定义消息）
     * 
     * @param message 响应消息
     * @param data 响应数据
     * @param <T> 数据类型
     * @return 响应对象
     */
    public static <T> ResponseDTO<T> success(String message, T data) {
        return new ResponseDTO<T>(200, message, data);
    }
    
    /**
     * 错误响应
     * 
     * @param code HTTP状态码
     * @param message 错误消息
     * @param <T> 数据类型
     * @return 响应对象
     */
    public static <T> ResponseDTO<T> error(Integer code, String message) {
        return new ResponseDTO<T>(code, message, null);
    }
}
