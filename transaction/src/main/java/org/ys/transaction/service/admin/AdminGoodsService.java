package org.ys.transaction.service.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.ys.commens.entity.YsGoods;
import org.ys.commens.pojo.CommentResult;

import java.util.List;

/**
 * 后台商品管理服务接口
 */
public interface AdminGoodsService {

    /**
     * 获取商品列表（分页）
     */
    CommentResult getGoodsList(String keyword, String category, String status, Integer pageNum, Integer pageSize);

    /**
     * 添加商品
     */
    CommentResult addGoods(YsGoods goods);

    /**
     * 更新商品
     */
    CommentResult updateGoods(YsGoods goods);

    /**
     * 删除商品
     */
    CommentResult deleteGoods(Long id);

    /**
     * 批量删除商品
     */
    CommentResult batchDeleteGoods(List<Long> ids);

    /**
     * 更新商品状态（上架/下架）
     */
    CommentResult updateGoodsStatus(Long id, Integer status);

    /**
     * 更新商品库存
     */
    CommentResult updateStock(Long goodsId, Integer stockChange);

    /**
     * 获取商品详情
     */
    CommentResult getGoodsDetail(Long id);

    /**
     * 批量导入商品
     */
    CommentResult batchImportGoods(List<YsGoods> goodsList);

    /**
     * 导出商品
     */
    CommentResult exportGoods(String category);
}
