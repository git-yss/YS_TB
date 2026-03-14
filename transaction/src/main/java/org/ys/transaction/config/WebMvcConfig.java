package org.ys.transaction.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.ys.transaction.interceptor.RateLimitInterceptor;

/**
 * Web MVC配置类
 * 配置拦截器
 *
 * @author makejava
 * @since 2025-07-16
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private RateLimitInterceptor rateLimitInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 添加限流拦截器
        registry.addInterceptor(rateLimitInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/static/**", "/login.html");
    }
}
