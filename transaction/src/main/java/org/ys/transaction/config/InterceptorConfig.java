package org.ys.transaction.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 拦截器配置
 */
@Configuration
public class InterceptorConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 后台管理拦截器
        registry.addInterceptor(new org.ys.transaction.interceptor.AdminInterceptor())
                .addPathPatterns("/admin/**")
                .excludePathPatterns("/admin/login", "/admin/config");
    }
}
