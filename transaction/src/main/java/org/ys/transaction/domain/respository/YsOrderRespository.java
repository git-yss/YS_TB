package org.ys.transaction.domain.respository;

import org.ys.transaction.domain.aggregate.OrderAggregate;
import org.ys.transaction.domain.aggregate.PayAggregate;

import java.util.List;

/**
 * (YsOrder)表数据库访问层
 *
 * @author makejava
 * @since 2025-08-31 12:34:47
 */
public interface YsOrderRespository {

/**
* 批量新增数据（MyBatis原生foreach方法）
*
* @param entities List<YsOrder> 实例对象列表
* @return 影响行数
*/
int insertBatch(List<OrderAggregate> entities);

/**
* 批量新增或按主键更新数据（MyBatis原生foreach方法）
*
* @param entities List<OrderAggregate> 实例对象列表
* @return 影响行数
* @throws org.springframework.jdbc.BadSqlGrammarException 入参是空List的时候会抛SQL语句错误的异常，请自行校验入参
*/
int insertOrUpdateBatch(List<OrderAggregate> entities);

int addOrder(OrderAggregate aggregate);

int updateStatusById(OrderAggregate aggregate);

int deleteById(OrderAggregate aggregate);

List<OrderAggregate> selectsById(OrderAggregate aggregate);

List<OrderAggregate> selectsByUserId(OrderAggregate aggregate);

OrderAggregate selectDetailById(OrderAggregate aggregate);

OrderAggregate selectAggregateById(OrderAggregate aggregate);

PayAggregate selectPayAggregateById(PayAggregate aggregate);
}

