package org.ys.transaction.service.admin;

import org.ys.commens.pojo.CommentResult;

/**
 * 后台统计数据服务接口
 */
public interface AdminStatisticsService {

    /**
     * 获取后台首页统计数据
     */
    CommentResult getDashboardStats();
}
