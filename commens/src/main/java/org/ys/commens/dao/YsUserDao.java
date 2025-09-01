package org.ys.commens.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;
import org.ys.commens.entity.YsGoods;
import org.ys.commens.entity.YsUser;

import java.util.List;
import java.util.Map;


/**
 * (YsUser)表数据库访问层
 *
 * @author makejava
 * @since 2025-07-16 19:41:45
 */
public interface YsUserDao extends BaseMapper<YsUser> {
    YsUser  queryUser(@Param("username") String username,@Param("password") String password);
    YsUser  selectById(Long id);
    int  updateBalanceById(@Param("ysUser") YsUser ysUser);

    List<YsGoods> queryAllGoodsPage(@Param("page") Page<YsGoods> page, @Param("map")  Map<String, Object> map);
}

