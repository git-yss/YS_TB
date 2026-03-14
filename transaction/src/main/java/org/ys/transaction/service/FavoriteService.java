package org.ys.transaction.service;

import org.ys.commens.pojo.CommentResult;

/**
 * 商品收藏服务接口
 *
 * @author makejava
 * @since 2025-07-16
 */
public interface FavoriteService {

    /**
     * 添加收藏
     * @param userId 用户ID
     * @param goodsId 商品ID
     * @return 收藏结果
     */
    CommentResult addFavorite(Long userId, Long goodsId);

    /**
     * 取消收藏
     * @param userId 用户ID
     * @param goodsId 商品ID
     * @return 取消结果
     */
    CommentResult removeFavorite(Long userId, Long goodsId);

    /**
     * 检查是否已收藏
     * @param userId 用户ID
     * @param goodsId 商品ID
     * @return 是否收藏
     */
    CommentResult checkFavorite(Long userId, Long goodsId);

    /**
     * 获取用户收藏列表
     * @param userId 用户ID
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @return 收藏列表
     */
    CommentResult getUserFavorites(Long userId, Integer pageNum, Integer pageSize);

    /**
     * 批量取消收藏
     * @param userId 用户ID
     * @param goodsIds 商品ID列表
     * @return 取消结果
     */
    CommentResult batchRemoveFavorites(Long userId, String goodsIds);
}
