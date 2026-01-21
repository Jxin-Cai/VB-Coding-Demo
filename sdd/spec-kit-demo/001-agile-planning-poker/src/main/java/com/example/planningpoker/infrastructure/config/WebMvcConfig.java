package com.example.planningpoker.infrastructure.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;

import java.io.IOException;

/**
 * Web MVC 配置
 * 
 * <p>支持 Vue Router 的 history 模式
 * <p>所有非API和非静态资源的请求都返回 index.html
 * 
 * @author 开发团队
 * @since 1.0.0
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    /**
     * 配置资源处理器
     * 
     * <p>为了支持 Vue Router 的 history 模式,所有不是API请求或静态资源的请求
     * 都应该返回 index.html,让前端路由处理
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 配置静态资源处理,支持前端路由
        registry.addResourceHandler("/**")
            .addResourceLocations("classpath:/static/")
            .resourceChain(true)
            .addResolver(new PathResourceResolver() {
                @Override
                protected Resource getResource(String resourcePath, Resource location) throws IOException {
                    Resource requestedResource = location.createRelative(resourcePath);
                    
                    // 如果请求的资源存在,直接返回
                    if (requestedResource.exists() && requestedResource.isReadable()) {
                        return requestedResource;
                    }
                    
                    // 如果是API请求或WebSocket请求,不处理
                    if (resourcePath.startsWith("api/") || resourcePath.startsWith("ws/") || 
                        resourcePath.startsWith("h2-console/") || resourcePath.startsWith("swagger-ui/") ||
                        resourcePath.startsWith("v3/api-docs")) {
                        return null;
                    }
                    
                    // 否则返回 index.html (支持前端路由)
                    return new ClassPathResource("/static/index.html");
                }
            });
    }
}
