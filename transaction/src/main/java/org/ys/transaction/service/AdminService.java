package org.ys.transaction.service;

import org.ys.commens.pojo.CommentResult;

import java.util.Map;

/**
 * 后台管理服务接口
 *
 * @author makejava
 * @since 2025-07-16
 */
public interface AdminService {

    // ========== 商品管理 ==========

    /**
     * 添加商品
     * @param params 商品信息
     * @return 添加结果
     */
    CommentResult addGoods(Map<String, Object> params);

    /**
     * 更新商品
     * @param params 商品信息
     * @return 更新结果
     */
    CommentResult updateGoods(Map<String, Object> params);

    /**
     * 删除商品
     * @param goodsId 商品ID
     * @return 删除结果
     */
    CommentResult deleteGoods(Long goodsId);

    /**
     * 上架/下架商品
     * @param goodsId 商品ID
     * @param status 状态（1=上架，0=下架）
     * @return 操作结果
     */
    CommentResult updateGoodsStatus(Long goodsId, Integer status);

    /**
     * 获取商品列表（分页）
     * @param params 查询参数
     * @return 商品列表
     */
    CommentResult getGoodsList(Map<String, Object> params);

    // ========== 订单管理 ==========

    /**
     * 获取订单列表（分页）
     * @param params 查询参数
     * @return 订单列表
     */
    CommentResult getOrderList(Map<String, Object> params);

    /**
     * 发货
     * @param orderId 订单ID
     * @param logisticsNo 物流单号
     * @param logisticsCompany 物流公司
     * @return 发货结果
     */
    CommentResult shipOrder(Long orderId, String logisticsNo, String logisticsCompany);

    /**
     * 处理退款
     * @param orderId 订单ID
     * @param approve 是否同意
     * @param remark 备注
     * @return 处理结果
     */
    CommentResult handleRefund(Long orderId, Boolean approve, String remark);

    // ========== 用户管理 ==========

    /**
     * 获取用户列表（分页）
     * @param params 查询参数
     * @return 用户列表
     */
    CommentResult getUserList(Map<String, Object> params);

    /**
     * 禁用/启用用户
     * @param userId 用户ID
     * @param status 状态（0=禁用，1=启用）
     * @return 操作结果
     */
    CommentResult updateUserStatus(Long userId, Integer status);

    /**
     * 重置用户密码
     * @param userId 用户ID
     * @param newPassword 新密码
     * @return 重置结果
     */
    CommentResult resetUserPassword(Long userId, String newPassword);

    // ========== 数据统计 ==========

    /**
     * 获取统计数据
     * @return 统计信息
     */
    CommentResult getStatistics();

    /**
     * 获取销售报表
     * @param params 查询参数（日期范围等）
     * @return 销售数据
     */
    CommentResult getSalesReport(Map<String, Object> params);

    /**
     * 获取用户分析数据
     * @param params 查询参数
     * @return 用户分析数据
     */
    CommentResult getUserAnalysis(Map<String, Object> params);

    // ========== 系统配置 ==========

    /**
     * 获取系统配置列表
     * @return 配置列表
     */
    CommentResult getConfigList();

    /**
     * 更新系统配置
     * @param params 配置参数
     * @return 更新结果
     */
    CommentResult updateConfig(Map<String, Object> params);

    // ========== 营销活动管理 ==========

    /**
     * 创建秒杀活动
     * @param params 秒杀参数
     * @return 创建结果
     */
    CommentResult createSeckillActivity(Map<String, Object> params);

    /**
     * 更新秒杀活动
     * @param params 秒杀参数
     * @return 更新结果
     */
    CommentResult updateSeckillActivity(Map<String, Object> params);

    /**
     * 删除秒杀活动
     * @param activityId 活动ID
     * @return 删除结果
     */
    CommentResult deleteSeckillActivity(Long activityId);

    /**
     * 获取秒杀活动列表
     * @param params 查询参数
     * @return 活动列表
     */
    CommentResult getSeckillActivityList(Map<String, Object> params);

    /**
     * 启动秒杀活动
     * @param activityId 活动ID
     * @return 启动结果
     */
    CommentResult startSeckillActivity(Long activityId);

    /**
     * 停止秒杀活动
     * @param activityId 活动ID
     * @return 停止结果
     */
    CommentResult stopSeckillActivity(Long activityId);
}
