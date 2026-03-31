package org.ys.transaction.domain.inteface;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.ys.transaction.Infrastructure.pojo.YsGoods;

import java.util.Map;

public interface IndexService {
    Map<String, Object> login(Map<String, Object> map);

    Page<YsGoods> queryAllGoods(Map<String, Object> map);
}
