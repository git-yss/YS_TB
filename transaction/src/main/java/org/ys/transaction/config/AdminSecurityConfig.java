package org.ys.transaction.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * 后台管理系统安全配置
 */
@Configuration
public class AdminSecurityConfig {

    /**
     * 仅对 /admin/** 使用独立的安全链，避免和 SecurityConfig 里
     * 的 SecurityFilterChain 风格冲突（不能同时使用 WebSecurityConfigurerAdapter）。
     */
    @Bean
    @org.springframework.core.annotation.Order(1)
    public SecurityFilterChain adminFilterChain(HttpSecurity http) throws Exception {
        http
                // 只匹配后台管理路径
                .antMatcher("/admin/**")
                // 后台鉴权逻辑由 AdminInterceptor 基于 session 完成
                .authorizeRequests()
                .anyRequest().permitAll()
                .and()
                // CSRF 与前后端分离场景不兼容，这里禁用
                .csrf().disable()
                // 允许 session，使 AdminInterceptor 的 session 属性可用
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED);

        return http.build();
    }
}
