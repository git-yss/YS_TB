package org.ys.transaction.service;

import org.ys.commens.pojo.CommentResult;

import java.util.List;
import java.util.Map;

/**
 * 商品服务接口
 *
 * @author makejava
 * @since 2025-07-16
 */
public interface ProductService {

    /**
     * 多条件搜索商品（分页）
     *
     * filters:
     * - keyword: 关键词
     * - categoryId: 分类ID
     * - brand: 品牌（可选）
     * - priceMin: 最低价格（可选）
     * - priceMax: 最高价格（可选）
     * - inventoryMin: 最小库存（可选）
     * - sort: 排序方案（可选）
     * - pageNum / pageSize: 分页参数
     * @return 搜索结果
     */
    CommentResult searchProducts(Map<String, Object> filters);

    /**
     * 获取商品分类列表
     * @return 分类列表
     */
    CommentResult getCategoryList();

    /**
     * 获取子分类
     * @param parentId 父分类ID
     * @return 子分类列表
     */
    CommentResult getSubCategories(Long parentId);

    /**
     * 获取商品详情
     * @param goodsId 商品ID
     * @return 商品详情
     */
    CommentResult getGoodsDetail(Long goodsId);

    /**
     * 获取分类下的商品列表
     * @param categoryId 分类ID
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @return 商品列表
     */
    CommentResult getGoodsByCategory(Long categoryId, Integer pageNum, Integer pageSize);

    /**
     * 获取热门商品
     * @param limit 数量限制
     * @return 商品列表
     */
    CommentResult getHotGoods(Integer limit);
}
