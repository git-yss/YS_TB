package org.ys.transaction.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.ys.commens.dao.YsCategoryDao;
import org.ys.commens.dao.YsGoodsDao;
import org.ys.commens.entity.YsGoods;
import org.ys.commens.pojo.CommentResult;
import org.ys.transaction.service.ProductService;
import org.springframework.stereotype.Service;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    @Resource
    private YsCategoryDao ysCategoryDao;

    @Resource
    private JdbcTemplate jdbcTemplate;

    @Override
    public CommentResult searchProducts(Map<String, Object> filters) {
        try {
            if (filters == null) {
                filters = Collections.emptyMap();
            }

            // 分页参数（默认值）
            Integer pageNum = filters.get("pageNum") != null ? Integer.valueOf(filters.get("pageNum").toString()) : 1;
            Integer pageSize = filters.get("pageSize") != null ? Integer.valueOf(filters.get("pageSize").toString()) : 10;
            if (pageNum < 1) pageNum = 1;
            if (pageSize < 1) pageSize = 10;

            // 构建查询条件
            Map<String, Object> queryMap = new HashMap<>();
            String keyword = filters.get("keyword") != null ? filters.get("keyword").toString() : null;
            if (keyword != null && !keyword.trim().isEmpty()) {
                queryMap.put("searchKeyword", keyword.trim());
            }
            Long categoryId = filters.get("categoryId") != null ? Long.valueOf(filters.get("categoryId").toString()) : null;
            if (categoryId != null && categoryId > 0) {
                queryMap.put("categoryId", categoryId);
            }

            String brand = filters.get("brand") != null ? filters.get("brand").toString() : null;
            if (brand != null && !brand.trim().isEmpty()) {
                queryMap.put("brand", brand.trim());
            }

            if (filters.get("priceMin") != null && !filters.get("priceMin").toString().trim().isEmpty()) {
                queryMap.put("priceMin", new java.math.BigDecimal(filters.get("priceMin").toString()));
            }
            if (filters.get("priceMax") != null && !filters.get("priceMax").toString().trim().isEmpty()) {
                queryMap.put("priceMax", new java.math.BigDecimal(filters.get("priceMax").toString()));
            }
            if (filters.get("inventoryMin") != null && !filters.get("inventoryMin").toString().trim().isEmpty()) {
                queryMap.put("inventoryMin", Integer.valueOf(filters.get("inventoryMin").toString()));
            }

            // 排序方案（字段白名单）
            String sort = filters.get("sort") != null ? filters.get("sort").toString() : null;
            String sortColumn = null;
            String sortOrder = "DESC";
            if (sort != null) {
                switch (sort) {
                    case "priceAsc":
                        sortColumn = "price";
                        sortOrder = "ASC";
                        break;
                    case "priceDesc":
                        sortColumn = "price";
                        sortOrder = "DESC";
                        break;
                    case "inventoryAsc":
                        sortColumn = "inventory";
                        sortOrder = "ASC";
                        break;
                    case "inventoryDesc":
                        sortColumn = "inventory";
                        sortOrder = "DESC";
                        break;
                    case "newest":
                        sortColumn = "id";
                        sortOrder = "DESC";
                        break;
                    default:
                        break;
                }
            }
            if (sortColumn != null) {
                queryMap.put("sortColumn", sortColumn);
                queryMap.put("sortOrder", sortOrder);
            }

            Page<YsGoods> page = new Page<>(pageNum, pageSize);
            IPage<YsGoods> resultPage = ysGoodsDao.queryAllGoodsPage(page, queryMap);

            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("list", resultPage.getRecords());
            resultMap.put("total", resultPage.getTotal());
            resultMap.put("pageNum", resultPage.getCurrent());
            resultMap.put("pageSize", resultPage.getSize());
            resultMap.put("pages", resultPage.getPages());

            log.info("商品搜索成功: keyword={}, categoryId={}, total={}", keyword, categoryId, resultPage.getTotal());
            return CommentResult.success(resultMap);
        } catch (Exception e) {
            log.error("商品搜索失败: {}", e.getMessage(), e);
            return CommentResult.error("商品搜索失败: " + e.getMessage());
        }
    }

    @Override
    public CommentResult getCategoryList() {
        try {
            // 注意：MyBatis-Plus 配置了逻辑删除字段 status（status=1 会被当作删除过滤）。
            // 但 ys_category.status=1 又表示启用，所以用 JdbcTemplate 直查绕开逻辑删除拦截。
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                    "select id, category_name from ys_category " +
                            "where status = 1 and parent_id = 0 " +
                            "order by sort_order asc"
            );
            return CommentResult.success(rows.stream().map(r -> {
                Map<String, Object> m = new HashMap<>();
                m.put("id", r.get("id"));
                m.put("name", r.get("category_name"));
                return m;
            }).collect(Collectors.toList()));
        } catch (Exception e) {
            log.error("获取商品分类列表失败: {}", e.getMessage(), e);
            return CommentResult.error("获取商品分类列表失败: " + e.getMessage());
        }
    }

    @Override
    public CommentResult getSubCategories(Long parentId) {
        try {
            if (parentId == null || parentId <= 0) {
                return CommentResult.error("parentId不能为空");
            }

            // 同理，绕开逻辑删除拦截
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                    "select id, category_name from ys_category " +
                            "where status = 1 and parent_id = ? " +
                            "order by sort_order asc",
                    parentId
            );
            return CommentResult.success(rows.stream().map(r -> {
                Map<String, Object> m = new HashMap<>();
                m.put("id", r.get("id"));
                m.put("name", r.get("category_name"));
                return m;
            }).collect(Collectors.toList()));
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
            return CommentResult.success(goods);
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
            return CommentResult.success(resultMap);
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
            return CommentResult.success(resultPage.getRecords());
        } catch (Exception e) {
            log.error("获取热门商品失败: limit={}, error={}", limit, e.getMessage(), e);
            return CommentResult.error("获取热门商品失败: " + e.getMessage());
        }
    }
}
