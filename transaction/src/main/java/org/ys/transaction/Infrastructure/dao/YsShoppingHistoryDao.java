package org.ys.transaction.Infrastructure.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.ys.transaction.Infrastructure.pojo.YsShoppingHistory;


/**
 * (YsShoppingHistory)表数据库访问层
 *
 * @author makejava
 * @since 2025-07-16 19:41:25
 */
public interface YsShoppingHistoryDao extends BaseMapper<YsShoppingHistory> {
    int insert(YsShoppingHistory ysShoppingHistory);

}

