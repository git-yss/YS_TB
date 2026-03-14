package org.ys.transaction.interceptor;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * 后台管理拦截器
 */
@Component
public class AdminInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String uri = request.getRequestURI();

        // 登录接口放行
        if (uri.equals("/admin/login") || uri.equals("/admin/config")) {
            return true;
        }

        // 检查session中是否有admin用户
        HttpSession session = request.getSession();
        Object adminUser = session.getAttribute("adminUser");

        if (adminUser == null) {
            response.setStatus(401);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"message\":\"请先登录\"}");
            return false;
        }

        return true;
    }
}
