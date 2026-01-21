package com.example.planningpoker.infrastructure.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 应用配置类
 * 
 * <p>配置MyBatis Mapper扫描路径、定时任务和其他Bean定义。
 * 
 * @author 开发团队
 * @since 1.0.0
 * @date 2026-01-18
 */
@Configuration
@MapperScan("com.example.planningpoker.infrastructure.persistence")
@EnableScheduling // 启用定时任务支持（用于投票倒计时）
public class AppConfig {
    // Bean定义在这里配置（如需要）
}
