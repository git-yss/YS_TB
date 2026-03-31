package org.ys.transaction.Infrastructure.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.ys.transaction.Infrastructure.pojo.YsProductFavorite;

import java.util.List;

/**
 * (YsProductFavorite)表数据库访问层
 *
 * @author makejava
 * @since 2025-07-16
 */
public interface YsProductFavoriteDao extends BaseMapper<YsProductFavorite> {

    /**
     * 查询用户收藏列表
     */
    List<YsProductFavorite> selectByUserId(@Param("userId") Long userId);

    /**
     * 检查是否已收藏
     */
    YsProductFavorite selectByUserAndGoods(@Param("userId") Long userId, @Param("goodsId") Long goodsId);
}
