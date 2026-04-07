package org.ys.transaction.Interface;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.ys.transaction.Interface.VO.CommentResult;
import org.ys.transaction.application.ProductApplicationService;

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
    private ProductApplicationService productApplicationService;

    /**
     * 搜索商品
     * @param map 包含keyword(关键词), categoryId(分类ID), pageNum(页码), pageSize(每页数量)
     * @return 搜索结果
     */
    @RequestMapping("search")
    @ResponseBody
    public CommentResult searchProducts(@RequestBody Map<String, Object> map) {
        try {
            return CommentResult.success(productApplicationService.searchProducts(map));
        } catch (Exception e) {
            return CommentResult.error(e.getMessage());
        }
    }

    /**
     * 获取商品分类列表
     * @return 分类列表
     */
    @RequestMapping("categoryList")
    @ResponseBody
    public CommentResult getCategoryList() {
        try {
            return CommentResult.success(productApplicationService.getCategoryList());
        } catch (Exception e) {
            return CommentResult.error(e.getMessage());
        }
    }

    /**
     * 获取子分类
     * @param map 包含parentId(父分类ID)
     * @return 子分类列表
     */
    @RequestMapping("subCategories")
    @ResponseBody
    public CommentResult getSubCategories(@RequestBody Map<String, Object> map) {
        try {
            if (map.get("parentId") == null) {
                throw new IllegalArgumentException("parentId不能为空");
            }
            Long parentId = Long.valueOf(map.get("parentId").toString());
            if (parentId <= 0) {
                throw new IllegalArgumentException("parentId不能为空");
            }
            return CommentResult.success(productApplicationService.getSubCategories(parentId));
        } catch (Exception e) {
            return CommentResult.error(e.getMessage());
        }
    }

    /**
     * 获取商品详情
     * @param map 包含goodsId(商品ID)
     * @return 商品详情
     */
    @RequestMapping("detail")
    @ResponseBody
    public CommentResult getGoodsDetail(@RequestBody Map<String, Object> map) {
        try {
            if (map.get("goodsId") == null) {
                throw new IllegalArgumentException("商品ID不能为空");
            }
            Long goodsId = Long.valueOf(map.get("goodsId").toString());
            if (goodsId <= 0) {
                throw new IllegalArgumentException("商品ID不能为空");
            }
            return CommentResult.success(productApplicationService.getGoodsDetail(goodsId));
        } catch (Exception e) {
            return CommentResult.error(e.getMessage());
        }
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
        try {
            return CommentResult.success(productApplicationService.getGoodsByCategory(categoryId, pageNum, pageSize));
        } catch (Exception e) {
            return CommentResult.error(e.getMessage());
        }
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
        try {
            return CommentResult.success(productApplicationService.getHotGoods(limit));
        } catch (Exception e) {
            return CommentResult.error(e.getMessage());
        }
    }

    /**
     * 重建商品 ES 索引（用于全量初始化/重建）
     */
    @PostMapping("es/rebuildIndex")
    @ResponseBody
    public CommentResult rebuildEsIndex() {
        try {
            return CommentResult.success(productApplicationService.rebuildEsGoodsIndex());
        } catch (Exception e) {
            return CommentResult.error(e.getMessage());
        }
    }

    /**
     * 增量同步单个商品到 ES（商品变更后可调用）
     */
    @PostMapping("es/syncByGoodsId")
    @ResponseBody
    public CommentResult syncEsByGoodsId(@RequestBody Map<String, Object> map) {
        try {
            if (map.get("goodsId") == null) {
                throw new IllegalArgumentException("goodsId不能为空");
            }
            Long goodsId = Long.valueOf(map.get("goodsId").toString());
            return CommentResult.success(productApplicationService.syncEsGoodsById(goodsId));
        } catch (Exception e) {
            return CommentResult.error(e.getMessage());
        }
    }

    /**
     * 搜索联想词（下拉建议）
     */
    @GetMapping("es/suggest")
    @ResponseBody
    public CommentResult esSuggest(
            @RequestParam("prefix") String prefix,
            @RequestParam(value = "size", required = false) Integer size
    ) {
        try {
            return CommentResult.success(productApplicationService.esSuggest(prefix, size));
        } catch (Exception e) {
            return CommentResult.error(e.getMessage());
        }
    }

    /**
     * 热门搜索词统计（进程内）
     */
    @GetMapping("es/hotKeywords")
    @ResponseBody
    public CommentResult esHotKeywords(@RequestParam(value = "size", required = false) Integer size) {
        try {
            return CommentResult.success(productApplicationService.esHotKeywords(size));
        } catch (Exception e) {
            return CommentResult.error(e.getMessage());
        }
    }

    /**
     * 查看商品搜索别名当前指向的物理索引（用于面试讲解/可运维）
     */
    @GetMapping("es/aliasStatus")
    @ResponseBody
    public CommentResult esAliasStatus() {
        try {
            return CommentResult.success(productApplicationService.esGoodsAliasStatus());
        } catch (Exception e) {
            return CommentResult.error(e.getMessage());
        }
    }
}
