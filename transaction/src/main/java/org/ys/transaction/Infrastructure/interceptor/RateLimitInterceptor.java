package org.ys.transaction.Infrastructure.interceptor;

import org.redisson.api.RBucket;
import org.redisson.api.RRateLimiter;
import org.redisson.api.RateIntervalUnit;
import org.redisson.api.RateType;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * 限流拦截器
 * 基于Redis实现接口访问频率限制（防刷机制）
 *
 * @author makejava
 * @since 2025-07-16
 */
@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    @Autowired
    private RedissonClient redissonClient;

    // 每秒最大请求数
    private static final int DEFAULT_RATE = 100;

    // 时间窗口大小（秒）
    private static final int DEFAULT_RATE_INTERVAL = 1;

    // IP限流：每个IP每秒最多请求次数
    private static final int IP_RATE_LIMIT = 50;

    // 用户限流：每个用户每秒最多请求次数
    private static final int USER_RATE_LIMIT = 30;

    // 秒杀接口特殊限流
    private static final int SECKILL_RATE_LIMIT = 5;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
        String uri = request.getRequestURI();
        String ip = getClientIp(request);
        String userId = request.getHeader("userId");

        // 对秒杀接口进行特殊限流
        if (uri.contains("/seckill")) {
            if (!rateLimit(ip, uri, SECKILL_RATE_LIMIT)) {
                sendErrorResponse(response, 429, "秒杀请求过于频繁，请稍后重试");
                return false;
            }
            if (userId != null && !rateLimit(userId, "user:seckill", SECKILL_RATE_LIMIT)) {
                sendErrorResponse(response, 429, "您秒杀操作过于频繁，请稍后重试");
                return false;
            }
            return true;
        }

        // IP限流
        if (!rateLimit(ip, uri, IP_RATE_LIMIT)) {
            sendErrorResponse(response, 429, "请求过于频繁，请稍后重试");
            return false;
        }

        // 用户限流
        if (userId != null && !rateLimit(userId, "user:request", USER_RATE_LIMIT)) {
            sendErrorResponse(response, 429, "您的操作过于频繁，请稍后重试");
            return false;
        }

        return true;
    }

    /**
     * 基于Redis实现令牌桶算法限流
     *
     * @param key    限流key
     * @param uri    请求URI
     * @param rate   限流速率
     * @return 是否允许访问
     */
    private boolean rateLimit(String key, String uri, int rate) {
        String rateLimiterKey = "rate:limit:" + key + ":" + uri;

        RRateLimiter rateLimiter = redissonClient.getRateLimiter(rateLimiterKey);
        rateLimiter.trySetRate(RateType.OVERALL, rate, DEFAULT_RATE_INTERVAL, RateIntervalUnit.SECONDS);

        return rateLimiter.tryAcquire(1);
    }

    /**
     * 获取客户端真实IP
     *
     * @param request 请求对象
     * @return 客户端IP地址
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 处理多级代理的情况
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

    /**
     * 发送错误响应
     *
     * @param response 响应对象
     * @param status   状态码
     * @param message  错误信息
     * @throws IOException IO异常
     */
    private void sendErrorResponse(HttpServletResponse response, int status, String message) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(status);
        response.getWriter().write("{\"code\":" + status + ",\"msg\":\"" + message + "\"}");
    }
}
