package com.example.planningpoker.presentation.exception;

import com.example.planningpoker.domain.exception.PoolNotFoundException;
import com.example.planningpoker.domain.exception.SessionAlreadyExistsException;
import com.example.planningpoker.domain.exception.SessionNotFoundException;
import com.example.planningpoker.domain.exception.StoryCardNotFoundException;
import com.example.planningpoker.domain.exception.UnauthorizedException;
import com.example.planningpoker.domain.exception.VotesNotCompleteException;
import com.example.planningpoker.presentation.dto.response.ResponseDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.stream.Collectors;

/**
 * 全局异常处理器
 * 
 * <p>统一处理所有Controller抛出的异常，返回友好的中文错误消息。
 * 
 * <p>异常类型：
 * <ul>
 *   <li>业务异常：404、409等</li>
 *   <li>验证异常：400参数验证失败</li>
 *   <li>领域规则异常：422业务规则不满足</li>
 *   <li>系统异常：500内部错误</li>
 * </ul>
 * 
 * @author 开发团队
 * @since 1.0.0
 * @date 2026-01-18
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    
    /**
     * 处理需求池未找到异常
     *
     * @param ex 异常
     * @return 错误响应
     */
    @ExceptionHandler(PoolNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseDTO<Void> handlePoolNotFound(PoolNotFoundException ex) {
        log.warn("需求池未找到: poolId={}", ex.getPoolId());
        return ResponseDTO.error(404, ex.getMessage());
    }

    /**
     * 处理故事卡未找到异常
     *
     * @param ex 异常
     * @return 错误响应
     */
    @ExceptionHandler(StoryCardNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseDTO<Void> handleStoryCardNotFound(StoryCardNotFoundException ex) {
        log.warn("故事卡未找到: storyCardId={}", ex.getStoryCardId());
        return ResponseDTO.error(404, ex.getMessage());
    }
    
    /**
     * 处理会话未找到异常
     * 
     * @param ex 异常
     * @return 错误响应
     */
    @ExceptionHandler(SessionNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseDTO<Void> handleSessionNotFound(SessionNotFoundException ex) {
        log.warn("估点会话未找到: {}", ex.getMessage());
        return ResponseDTO.error(404, ex.getMessage());
    }
    
    /**
     * 处理会话已存在异常
     * 
     * @param ex 异常
     * @return 错误响应
     */
    @ExceptionHandler(SessionAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ResponseDTO<Void> handleSessionAlreadyExists(SessionAlreadyExistsException ex) {
        log.warn("估点会话冲突: {}", ex.getMessage());
        return ResponseDTO.error(409, ex.getMessage());
    }
    
    /**
     * 处理投票未完成异常
     * 
     * @param ex 异常
     * @return 错误响应
     */
    @ExceptionHandler(VotesNotCompleteException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    public ResponseDTO<Void> handleVotesNotComplete(VotesNotCompleteException ex) {
        log.warn("投票未完成: {}", ex.getMessage());
        return ResponseDTO.error(422, ex.getMessage());
    }
    
    /**
     * 处理未授权异常
     * 
     * @param ex 异常
     * @return 错误响应
     */
    @ExceptionHandler(UnauthorizedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ResponseDTO<Void> handleUnauthorized(UnauthorizedException ex) {
        log.warn("权限不足: {}", ex.getMessage());
        return ResponseDTO.error(403, ex.getMessage());
    }
    
    /**
     * 处理领域规则异常（IllegalStateException）
     * 
     * @param ex 异常
     * @return 错误响应
     */
    @ExceptionHandler(IllegalStateException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    public ResponseDTO<Void> handleIllegalState(IllegalStateException ex) {
        log.warn("业务规则不满足: {}", ex.getMessage());
        return ResponseDTO.error(422, ex.getMessage());
    }
    
    /**
     * 处理参数验证异常
     * 
     * @param ex 异常
     * @return 错误响应
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseDTO<Void> handleValidationException(MethodArgumentNotValidException ex) {
        // 收集所有验证错误消息
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        
        log.warn("参数验证失败: {}", message);
        return ResponseDTO.error(400, "请求参数无效: " + message);
    }
    
    /**
     * 处理参数异常（IllegalArgumentException）
     * 
     * @param ex 异常
     * @return 错误响应
     */
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseDTO<Void> handleIllegalArgument(IllegalArgumentException ex) {
        log.warn("参数异常: {}", ex.getMessage());
        return ResponseDTO.error(400, ex.getMessage());
    }
    
    /**
     * 处理静态资源未找到异常（如favicon.ico）
     * 
     * <p>浏览器会自动请求favicon.ico，如果文件不存在会产生404，
     * 这是正常现象，不应记录为ERROR级别的错误。
     * 
     * @param ex 异常
     * @return 错误响应
     */
    @ExceptionHandler(NoResourceFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseDTO<Void> handleNoResourceFound(NoResourceFoundException ex) {
        // 只记录DEBUG级别日志，避免污染ERROR日志
        log.debug("静态资源未找到: {}", ex.getMessage());
        return ResponseDTO.error(404, "资源未找到");
    }
    
    /**
     * 处理所有未捕获的异常
     *
     * <p>这是最后的兜底处理，记录完整的堆栈跟踪以便调试。
     *
     * @param ex 异常
     * @return 错误响应
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseDTO<Void> handleGenericException(Exception ex) {
        log.error("系统内部错误: {} - {}", ex.getClass().getSimpleName(), ex.getMessage(), ex);

        // 开发环境返回详细错误信息，生产环境返回通用消息
        String errorMessage = "系统内部错误，请稍后重试";
        if (isDevelopmentProfile()) {
            errorMessage = String.format("系统错误: %s - %s",
                    ex.getClass().getSimpleName(),
                    ex.getMessage());
        }

        return ResponseDTO.error(500, errorMessage);
    }

    /**
     * 判断是否为开发环境
     *
     * @return true如果是开发环境
     */
    private boolean isDevelopmentProfile() {
        // 可以通过Spring的Environment获取，这里简化处理
        String profile = System.getProperty("spring.profiles.active");
        return "dev".equals(profile) || "development".equals(profile);
    }
}
