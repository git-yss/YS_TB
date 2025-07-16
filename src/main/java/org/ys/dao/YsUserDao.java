package org.ys.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.ys.entity.YsUser;

/**
 * (YsUser)表数据库访问层
 *
 * @author makejava
 * @since 2025-07-16 19:41:45
 */
@Mapper
public interface YsUserDao extends BaseMapper<YsUser> {
    YsUser  queryUser(String username, String password);

}

