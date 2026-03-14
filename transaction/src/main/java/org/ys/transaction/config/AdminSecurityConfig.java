package org.ys.transaction.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

/**
 * 后台管理系统安全配置
 */
@Configuration
@EnableWebSecurity
public class AdminSecurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .authorizeRequests()
                // 公开的接口
                .antMatchers("/admin/login").permitAll()
                .antMatchers("/admin/config").permitAll()
                // 需要管理员权限的接口
                .antMatchers("/admin/**").authenticated()
                .and()
                // 暂时禁用CSRF
                .csrf().disable()
                // 暂时使用基本认证（生产环境应该使用JWT）
                .httpBasic();
    }
}
