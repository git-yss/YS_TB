package org.ys.transaction.domain.inteface.admin;

import org.ys.transaction.domain.vo.DomainResult;

/**
 * 后台统计数据服务接口
 */
public interface AdminStatisticsService {

    /**
     * 获取后台首页统计数据
     */
    DomainResult getDashboardStats();
}
