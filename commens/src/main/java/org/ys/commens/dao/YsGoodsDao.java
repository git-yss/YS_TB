package org.ys.commens.dao;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.ys.commens.entity.YsGoods;

import java.math.BigDecimal;

/**
 * (YsGoods)表数据库访问层
 *
 * @author makejava
 * @since 2025-07-16 19:37:18
 */

public interface YsGoodsDao extends BaseMapper<YsGoods> {

    void decreaseStock(long itemId, Integer num);

    YsGoods selectGoodById(long itemId);

    void increaseStock(long itemId, Integer num);
}

