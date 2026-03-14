package org.ys.transaction.controller;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.ys.commens.pojo.CommentResult;
import org.ys.transaction.service.ProductService;

import javax.annotation.Resource;
import java.util.Map;

/**
 * 商品控制器
 *
 * @author system
 * @since 2025-03-14
 */
@RequestMapping("product")
@RestController
public class ProductController {

    @Resource
    private ProductService productService;

    /**
     * 搜索商品
     * @param map 包含keyword(关键词), categoryId(分类ID), pageNum(页码), pageSize(每页数量)
     * @return 搜索结果
     */
    @RequestMapping("search")
    @ResponseBody
    public CommentResult searchProducts(@RequestBody Map<String, Object> map) {
        String keyword = map.get("keyword") != null ? map.get("keyword").toString() : null;
        Long categoryId = map.get("categoryId") != null ? Long.valueOf(map.get("categoryId").toString()) : null;
        Integer pageNum = map.get("pageNum") != null ? Integer.valueOf(map.get("pageNum").toString()) : null;
        Integer pageSize = map.get("pageSize") != null ? Integer.valueOf(map.get("pageSize").toString()) : null;
        return productService.searchProducts(keyword, categoryId, pageNum, pageSize);
    }

    /**
     * 获取商品分类列表
     * @return 分类列表
     */
    @RequestMapping("categoryList")
    @ResponseBody
    public CommentResult getCategoryList() {
        return productService.getCategoryList();
    }

    /**
     * 获取子分类
     * @param map 包含parentId(父分类ID)
     * @return 子分类列表
     */
    @RequestMapping("subCategories")
    @ResponseBody
    public CommentResult getSubCategories(@RequestBody Map<String, Object> map) {
        Long parentId = map.get("parentId") != null ? Long.valueOf(map.get("parentId").toString()) : null;
        return productService.getSubCategories(parentId);
    }

    /**
     * 获取商品详情
     * @param map 包含goodsId(商品ID)
     * @return 商品详情
     */
    @RequestMapping("detail")
    @ResponseBody
    public CommentResult getGoodsDetail(@RequestBody Map<String, Object> map) {
        Long goodsId = Long.valueOf(map.get("goodsId").toString());
        return productService.getGoodsDetail(goodsId);
    }

    /**
     * 获取分类下的商品列表
     * @param map 包含categoryId(分类ID), pageNum(页码), pageSize(每页数量)
     * @return 商品列表
     */
    @RequestMapping("listByCategory")
    @ResponseBody
    public CommentResult getGoodsByCategory(@RequestBody Map<String, Object> map) {
        Long categoryId = map.get("categoryId") != null ? Long.valueOf(map.get("categoryId").toString()) : null;
        Integer pageNum = map.get("pageNum") != null ? Integer.valueOf(map.get("pageNum").toString()) : null;
        Integer pageSize = map.get("pageSize") != null ? Integer.valueOf(map.get("pageSize").toString()) : null;
        return productService.getGoodsByCategory(categoryId, pageNum, pageSize);
    }

    /**
     * 获取热门商品
     * @param map 包含limit(数量限制)
     * @return 热门商品列表
     */
    @RequestMapping("hotGoods")
    @ResponseBody
    public CommentResult getHotGoods(@RequestBody Map<String, Object> map) {
        Integer limit = map.get("limit") != null ? Integer.valueOf(map.get("limit").toString()) : null;
        return productService.getHotGoods(limit);
    }
}
