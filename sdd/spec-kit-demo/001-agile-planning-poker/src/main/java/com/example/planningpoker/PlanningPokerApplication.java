package com.example.planningpoker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 敏捷估点服务主应用类
 * 
 * <p>基于Spring Boot的Planning Poker估点工具，提供：
 * <ul>
 *   <li>故事卡管理：创建、查看、更新、删除故事卡</li>
 *   <li>估点会话：开始会话、投票、查看结果、完成会话</li>
 *   <li>实时通知：通过WebSocket推送估点进度和结果</li>
 * </ul>
 * 
 * <p>技术栈：
 * <ul>
 *   <li>后端：Java 17 + Spring Boot 3.x + MyBatis 3.x + H2</li>
 *   <li>前端：Vue.js 3.x + Element Plus + Vite</li>
 *   <li>实时通信：Spring WebSocket + STOMP</li>
 * </ul>
 * 
 * <p>架构：四层架构 + DDD领域驱动设计
 * 
 * @author 开发团队
 * @since 1.0.0
 * @date 2026-01-18
 */
@SpringBootApplication
public class PlanningPokerApplication {

    /**
     * 应用程序入口
     * 
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        SpringApplication.run(PlanningPokerApplication.class, args);
        
        // 打印启动信息
        System.out.println("============================================");
        System.out.println("敏捷估点服务已启动");
        System.out.println("访问地址: http://localhost:8080");
        System.out.println("H2控制台: http://localhost:8080/h2-console");
        System.out.println("API文档: http://localhost:8080/swagger-ui.html");
        System.out.println("============================================");
    }
}
