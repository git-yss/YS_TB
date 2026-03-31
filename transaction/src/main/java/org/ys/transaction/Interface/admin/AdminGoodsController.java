package org.ys.transaction.Interface.admin;

import org.springframework.web.bind.annotation.*;
import org.ys.commens.entity.YsGoods;
import org.ys.commens.pojo.CommentResult;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * 商品管理控制器
 */
@RestController
@RequestMapping("/admin/goods")
public class AdminGoodsController {

    @Resource
    private org.ys.transaction.domain.inteface.admin.AdminGoodsService adminGoodsService;

    /**
     * 获取商品列表（分页）
     */
    @PostMapping("/list")
    public CommentResult getGoodsList(@RequestBody Map<String, Object> params) {
        String keyword = params.get("keyword") != null ? params.get("keyword").toString() : null;
        String category = params.get("category") != null ? params.get("category").toString() : null;
        String status = params.get("status") != null ? params.get("status").toString() : null;
        Integer pageNum = params.get("pageNum") != null ? Integer.parseInt(params.get("pageNum").toString()) : 1;
        Integer pageSize = params.get("pageSize") != null ? Integer.parseInt(params.get("pageSize").toString()) : 10;
        return adminGoodsService.getGoodsList(keyword, category, status, pageNum, pageSize);
    }

    /**
     * 添加商品
     */
    @PostMapping("/add")
    public CommentResult addGoods(@RequestBody YsGoods goods) {
        return adminGoodsService.addGoods(goods);
    }

    /**
     * 更新商品
     */
    @PostMapping("/update")
    public CommentResult updateGoods(@RequestBody YsGoods goods) {
        return adminGoodsService.updateGoods(goods);
    }

    /**
     * 删除商品
     */
    @PostMapping("/delete/{id}")
    public CommentResult deleteGoods(@PathVariable Long id) {
        return adminGoodsService.deleteGoods(id);
    }

    /**
     * 批量删除商品
     */
    @PostMapping("/batchDelete")
    public CommentResult batchDeleteGoods(@RequestBody List<Long> ids) {
        return adminGoodsService.batchDeleteGoods(ids);
    }

    /**
     * 商品上架
     */
    @PostMapping("/shelve/{id}")
    public CommentResult shelveGoods(@PathVariable Long id) {
        return adminGoodsService.updateGoodsStatus(id, 1);
    }

    /**
     * 商品下架
     */
    @PostMapping("/unshelve/{id}")
    public CommentResult unshelveGoods(@PathVariable Long id) {
        return adminGoodsService.updateGoodsStatus(id, 0);
    }

    /**
     * 更新商品库存
     */
    @PostMapping("/stock")
    public CommentResult updateStock(@RequestBody Map<String, Object> params) {
        Long goodsId = Long.parseLong(params.get("goodsId").toString());
        Integer stockChange = Integer.parseInt(params.get("stockChange").toString());
        return adminGoodsService.updateStock(goodsId, stockChange);
    }

    /**
     * 获取商品详情
     */
    @GetMapping("/detail/{id}")
    public CommentResult getGoodsDetail(@PathVariable Long id) {
        return adminGoodsService.getGoodsDetail(id);
    }

    /**
     * 批量导入商品
     */
    @PostMapping("/import")
    public CommentResult importGoods(@RequestBody List<YsGoods> goodsList) {
        return adminGoodsService.batchImportGoods(goodsList);
    }

    /**
     * 导出商品
     */
    @GetMapping("/export")
    public CommentResult exportGoods(@RequestParam(required = false) String category) {
        return adminGoodsService.exportGoods(category);
    }
}
