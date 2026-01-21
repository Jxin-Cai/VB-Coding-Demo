package com.example.planningpoker.presentation.controller;

import com.example.planningpoker.application.service.ParticipantSessionService;
import com.example.planningpoker.presentation.dto.request.LoginRequestDTO;
import com.example.planningpoker.presentation.dto.response.LoginResponseDTO;
import com.example.planningpoker.presentation.dto.response.ResponseDTO;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

/**
 * 参与者Controller
 * 
 * <p>提供参与者相关的REST API端点：
 * <ul>
 *   <li>POST /api/v1/participants/login - 用户登录</li>
 * </ul>
 * 
 * @author 开发团队
 * @since 1.0.0
 * @date 2026-01-18
 */
@RestController
@RequestMapping("/api/v1/participants")
public class ParticipantController {
    
    private static final Logger log = LoggerFactory.getLogger(ParticipantController.class);
    private final ParticipantSessionService sessionService;
    
    public ParticipantController(ParticipantSessionService sessionService) {
        this.sessionService = sessionService;
    }
    
    /**
     * 用户登录
     * 
     * <p>通过用户名登录，无需密码。生成唯一的sessionToken用于后续API调用。
     * 
     * @param requestDTO 登录请求DTO
     * @return 登录响应（包含sessionToken和用户名）
     */
    @PostMapping("/login")
    public ResponseDTO<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO requestDTO) {
        log.info("接收登录请求，用户名: {}", requestDTO.getName());
        
        // 调用会话服务生成令牌
        String sessionToken = sessionService.login(requestDTO.getName());
        
        // 构建响应
        LoginResponseDTO responseDTO = new LoginResponseDTO(sessionToken, requestDTO.getName());
        
        return ResponseDTO.success("登录成功", responseDTO);
    }
}
