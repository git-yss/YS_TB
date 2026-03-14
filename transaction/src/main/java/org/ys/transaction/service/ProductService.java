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
     * 搜索商品
     * @param keyword 关键词
     * @param categoryId 分类ID
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @return 搜索结果
     */
    CommentResult searchProducts(String keyword, Long categoryId, Integer pageNum, Integer pageSize);

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
