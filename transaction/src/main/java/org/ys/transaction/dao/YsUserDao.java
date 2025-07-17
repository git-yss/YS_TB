package org.ys.transaction.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.ys.commens.entity.YsUser;


/**
 * (YsUser)表数据库访问层
 *
 * @author makejava
 * @since 2025-07-16 19:41:45
 */
public interface YsUserDao extends BaseMapper<YsUser> {
    YsUser  queryUser(String username, String password);

}

