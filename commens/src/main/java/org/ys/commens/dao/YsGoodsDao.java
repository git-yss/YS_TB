package org.ys.commens.dao;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;
import org.ys.commens.entity.YsGoods;

import java.math.BigDecimal;
import java.util.Map;

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

    Page<YsGoods> queryAllGoodsPage(@Param("page") IPage<YsGoods> page, @Param("map") Map<String, Object> map);
}

