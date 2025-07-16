package org.ys.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.ys.entity.YsGoods;

/**
 * (YsGoods)表数据库访问层
 *
 * @author makejava
 * @since 2025-07-16 19:37:18
 */
@Mapper
public interface YsGoodsDao extends BaseMapper<YsGoods> {

}

