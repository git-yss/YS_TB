package org.ys.transaction.Infrastructure.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.ys.transaction.Infrastructure.pojo.YsOrder;
import org.ys.transaction.domain.vo.CartItem;

import java.util.List;
import java.util.Map;

/**
 * (YsOrder)表数据库访问层
 *
 * @author makejava
 * @since 2025-08-31 12:34:47
 */
public interface YsOrderDao extends BaseMapper<YsOrder> {

/**
* 批量新增数据（MyBatis原生foreach方法）
*
* @param entities List<YsOrder> 实例对象列表
* @return 影响行数
*/
int insertBatch(@Param("entities") List<YsOrder> entities);

/**
* 批量新增或按主键更新数据（MyBatis原生foreach方法）
*
* @param entities List<YsOrder> 实例对象列表
* @return 影响行数
* @throws org.springframework.jdbc.BadSqlGrammarException 入参是空List的时候会抛SQL语句错误的异常，请自行校验入参
*/
int insertOrUpdateBatch(@Param("entities") List<YsOrder> entities);

int addOrder(@Param("cartItem") CartItem cartItem);

 int updateStatusById(@Param("code") int code,@Param("id") long id);

 int deleteById(@Param("id") long id,@Param("userId") long userId,@Param("itemId") long itemId,@Param("status") int status);

 List<YsOrder> selectsById(@Param("id") long id);

 List<YsOrder> selectsByUserId(@Param("userId") long userId);

 List<Map<String, Object>> selectDetailById(@Param("orderId")long orderId);
}

