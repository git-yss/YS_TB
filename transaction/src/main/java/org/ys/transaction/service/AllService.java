package org.ys.transaction.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.ys.commens.entity.YsGoods;

import java.util.List;
import java.util.Map;

public interface AllService {
    void login(Map<String, Object> map);

    Page<YsGoods> queryAllGoods(Map<String, Object> map);
}
