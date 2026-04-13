package org.ys.transaction.Infrastructure.dao;

import org.apache.ibatis.annotations.Param;
import org.ys.transaction.Infrastructure.pojo.YsRechargeOrder;

public interface YsRechargeOrderDao {
    int insert(@Param("entity") YsRechargeOrder entity);

    YsRechargeOrder selectByRechargeNo(@Param("rechargeNo") String rechargeNo);

    int markSuccess(@Param("rechargeNo") String rechargeNo,
                    @Param("tradeNo") String tradeNo);
}
