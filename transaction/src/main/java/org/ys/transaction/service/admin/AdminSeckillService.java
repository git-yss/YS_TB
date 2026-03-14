package org.ys.transaction.service.admin;

import org.ys.commens.pojo.CommentResult;

import java.util.Map;

/**
 * 后台秒杀管理服务接口
 */
public interface AdminSeckillService {

    /**
     * 获取秒杀活动列表
     */
    CommentResult getSeckillList(Integer pageNum, Integer pageSize);

    /**
     * 创建秒杀活动
     */
    CommentResult createSeckill(Map<String, Object> params);

    /**
     * 更新秒杀活动
     */
    CommentResult updateSeckill(Map<String, Object> params);

    /**
     * 删除秒杀活动
     */
    CommentResult deleteSeckill(Long id);

    /**
     * 更新秒杀活动状态
     */
    CommentResult updateSeckillStatus(Long id, Integer status);

    /**
     * 获取秒杀统计数据
     */
    CommentResult getSeckillStatistics();
}
