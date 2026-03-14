package org.ys.transaction.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.ys.commens.dao.YsGoodsDao;
import org.ys.commens.entity.YsGoods;
import org.ys.commens.pojo.CommentResult;
import org.ys.transaction.service.ProductService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 商品服务实现类
 *
 * @author system
 * @since 2025-03-14
 */
@Service
public class ProductServiceImpl implements ProductService {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ProductServiceImpl.class);

    @Resource
    private YsGoodsDao ysGoodsDao;

    @Override
    public CommentResult searchProducts(String keyword, Long categoryId, Integer pageNum, Integer pageSize) {
        try {
            // 设置默认分页参数
            if (pageNum == null || pageNum < 1) {
                pageNum = 1;
            }
            if (pageSize == null || pageSize < 1) {
                pageSize = 10;
            }

            // 构建查询条件
            Map<String, Object> queryMap = new HashMap<>();
            if (keyword != null && !keyword.trim().isEmpty()) {
                queryMap.put("searchKeyword", keyword.trim());
            }
            if (categoryId != null && categoryId > 0) {
                queryMap.put("categoryId", categoryId);
            }

            // 执行分页查询
            Page<YsGoods> page = new Page<>(pageNum, pageSize);
            IPage<YsGoods> resultPage = ysGoodsDao.queryAllGoodsPage(page, queryMap);

            // 构建返回结果
            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("list", resultPage.getRecords());
            resultMap.put("total", resultPage.getTotal());
            resultMap.put("pageNum", resultPage.getCurrent());
            resultMap.put("pageSize", resultPage.getSize());
            resultMap.put("pages", resultPage.getPages());

            log.info("商品搜索成功: keyword={}, categoryId={}, total={}", keyword, categoryId, resultPage.getTotal());
            return CommentResult.ok(resultMap);
        } catch (Exception e) {
            log.error("商品搜索失败: {}", e.getMessage(), e);
            return CommentResult.error("商品搜索失败: " + e.getMessage());
        }
    }

    @Override
    public CommentResult getCategoryList() {
        try {
            // 这里应该从category表查询，暂时返回空列表，待创建CategoryDao后完善
            log.info("获取商品分类列表成功");
            return CommentResult.ok("分类列表待实现");
        } catch (Exception e) {
            log.error("获取商品分类列表失败: {}", e.getMessage(), e);
            return CommentResult.error("获取商品分类列表失败: " + e.getMessage());
        }
    }

    @Override
    public CommentResult getSubCategories(Long parentId) {
        try {
            // 这里应该从category表查询，待创建CategoryDao后完善
            log.info("获取子分类成功: parentId={}", parentId);
            return CommentResult.ok("子分类列表待实现");
        } catch (Exception e) {
            log.error("获取子分类失败: parentId={}, error={}", parentId, e.getMessage(), e);
            return CommentResult.error("获取子分类失败: " + e.getMessage());
        }
    }

    @Override
    public CommentResult getGoodsDetail(Long goodsId) {
        try {
            if (goodsId == null || goodsId <= 0) {
                return CommentResult.error("商品ID不能为空");
            }

            YsGoods goods = ysGoodsDao.selectGoodById(goodsId);
            if (goods == null) {
                return CommentResult.error("商品不存在");
            }

            log.info("获取商品详情成功: goodsId={}", goodsId);
            return CommentResult.ok(goods);
        } catch (Exception e) {
            log.error("获取商品详情失败: goodsId={}, error={}", goodsId, e.getMessage(), e);
            return CommentResult.error("获取商品详情失败: " + e.getMessage());
        }
    }

    @Override
    public CommentResult getGoodsByCategory(Long categoryId, Integer pageNum, Integer pageSize) {
        try {
            // 设置默认分页参数
            if (pageNum == null || pageNum < 1) {
                pageNum = 1;
            }
            if (pageSize == null || pageSize < 1) {
                pageSize = 10;
            }

            // 构建查询条件
            Map<String, Object> queryMap = new HashMap<>();
            if (categoryId != null && categoryId > 0) {
                queryMap.put("categoryId", categoryId);
            }

            // 执行分页查询
            Page<YsGoods> page = new Page<>(pageNum, pageSize);
            IPage<YsGoods> resultPage = ysGoodsDao.queryAllGoodsPage(page, queryMap);

            // 构建返回结果
            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("list", resultPage.getRecords());
            resultMap.put("total", resultPage.getTotal());
            resultMap.put("pageNum", resultPage.getCurrent());
            resultMap.put("pageSize", resultPage.getSize());
            resultMap.put("pages", resultPage.getPages());

            log.info("获取分类商品成功: categoryId={}, total={}", categoryId, resultPage.getTotal());
            return CommentResult.ok(resultMap);
        } catch (Exception e) {
            log.error("获取分类商品失败: categoryId={}, error={}", categoryId, e.getMessage(), e);
            return CommentResult.error("获取分类商品失败: " + e.getMessage());
        }
    }

    @Override
    public CommentResult getHotGoods(Integer limit) {
        try {
            if (limit == null || limit < 1) {
                limit = 10;
            }
            if (limit > 100) {
                limit = 100; // 限制最大数量
            }

            // 查询所有商品并按销量或评分排序（这里简化处理，返回库存前limit的商品）
            Map<String, Object> queryMap = new HashMap<>();
            Page<YsGoods> page = new Page<>(1, limit);
            IPage<YsGoods> resultPage = ysGoodsDao.queryAllGoodsPage(page, queryMap);

            log.info("获取热门商品成功: limit={}", limit);
            return CommentResult.ok(resultPage.getRecords());
        } catch (Exception e) {
            log.error("获取热门商品失败: limit={}, error={}", limit, e.getMessage(), e);
            return CommentResult.error("获取热门商品失败: " + e.getMessage());
        }
    }
}
