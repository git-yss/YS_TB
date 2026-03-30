package org.ys.transaction.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.ys.commens.entity.YsGoods;
import org.ys.commens.pojo.CommentResult;

import java.util.Map;

public interface IndexService {
    CommentResult login(Map<String, Object> map);

    Page<YsGoods> queryAllGoods(Map<String, Object> map);
}
