package org.ys.transaction.domain.respository;

import org.ys.transaction.domain.aggregate.UserAddrAggregate;


/**
 * (YsUserAddr)表数据库访问层
 *
 * @author makejava
 * @since 2025-07-16 19:41:46
 */
public interface YsUserAddrRespository {
    int insert(UserAddrAggregate aggregate);
}

