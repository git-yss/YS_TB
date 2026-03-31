package org.ys.transaction.domain.inteface.admin;

import org.ys.commens.entity.YsGoods;
import org.ys.transaction.domain.vo.DomainResult;

import java.util.List;

/**
 * 后台商品管理服务接口
 */
public interface AdminGoodsService {

    /**
     * 获取商品列表（分页）
     */
    DomainResult getGoodsList(String keyword, String category, String status, Integer pageNum, Integer pageSize);

    /**
     * 添加商品
     */
    DomainResult addGoods(YsGoods goods);

    /**
     * 更新商品
     */
    DomainResult updateGoods(YsGoods goods);

    /**
     * 删除商品
     */
    DomainResult deleteGoods(Long id);

    /**
     * 批量删除商品
     */
    DomainResult batchDeleteGoods(List<Long> ids);

    /**
     * 更新商品状态（上架/下架）
     */
    DomainResult updateGoodsStatus(Long id, Integer status);

    /**
     * 更新商品库存
     */
    DomainResult updateStock(Long goodsId, Integer stockChange);

    /**
     * 获取商品详情
     */
    DomainResult getGoodsDetail(Long id);

    /**
     * 批量导入商品
     */
    DomainResult batchImportGoods(List<YsGoods> goodsList);

    /**
     * 导出商品
     */
    DomainResult exportGoods(String category);
}
