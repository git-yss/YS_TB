package org.ys.transaction.domain.inteface.admin;

import org.ys.transaction.domain.vo.DomainResult;

import java.util.Map;

/**
 * 后台秒杀管理服务接口
 */
public interface AdminSeckillService {

    /**
     * 获取秒杀活动列表
     */
    DomainResult getSeckillList(Integer pageNum, Integer pageSize);

    /**
     * 创建秒杀活动
     */
    DomainResult createSeckill(Map<String, Object> params);

    /**
     * 更新秒杀活动
     */
    DomainResult updateSeckill(Map<String, Object> params);

    /**
     * 删除秒杀活动
     */
    DomainResult deleteSeckill(Long id);

    /**
     * 更新秒杀活动状态
     */
    DomainResult updateSeckillStatus(Long id, Integer status);

    /**
     * 获取秒杀统计数据
     */
    DomainResult getSeckillStatistics();
}
