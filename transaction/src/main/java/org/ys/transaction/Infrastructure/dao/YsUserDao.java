package org.ys.transaction.Infrastructure.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.ys.transaction.Infrastructure.pojo.YsUser;


/**
 * (YsUser)表数据库访问层
 *
 * @author makejava
 * @since 2025-07-16 19:41:45
 */
public interface YsUserDao extends BaseMapper<YsUser> {
    YsUser  queryUser(@Param("username") String username,@Param("password") String password);
    YsUser  selectById(Long id);
    YsUser  selectByName(String username);
    YsUser  selectByEmail(String email);
    int  updateBalanceById(@Param("ysUser") YsUser ysUser);
    int  insert(@Param("entity") YsUser ysUser);

}

