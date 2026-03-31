package org.ys.transaction.domain.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.ys.transaction.domain.inteface.ProductService;
import org.springframework.stereotype.Service;
import org.springframework.jdbc.core.JdbcTemplate;
import org.ys.transaction.Infrastructure.dao.YsCategoryDao;
import org.ys.transaction.Infrastructure.dao.YsGoodsDao;
import org.ys.transaction.Infrastructure.pojo.YsGoods;

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
    public Map<String, Object> searchProducts(Map<String, Object> filters) {
        if (filters == null) filters = Collections.emptyMap();

        Integer pageNum = filters.get("pageNum") != null ? Integer.valueOf(filters.get("pageNum").toString()) : 1;
        Integer pageSize = filters.get("pageSize") != null ? Integer.valueOf(filters.get("pageSize").toString()) : 10;
        if (pageNum < 1) pageNum = 1;
        if (pageSize < 1) pageSize = 10;

        Map<String, Object> queryMap = new HashMap<>();
        String keyword = filters.get("keyword") != null ? filters.get("keyword").toString() : null;
        if (keyword != null && !keyword.trim().isEmpty()) queryMap.put("searchKeyword", keyword.trim());
        Long categoryId = filters.get("categoryId") != null ? Long.valueOf(filters.get("categoryId").toString()) : null;
        if (categoryId != null && categoryId > 0) queryMap.put("categoryId", categoryId);

        String brand = filters.get("brand") != null ? filters.get("brand").toString() : null;
        if (brand != null && !brand.trim().isEmpty()) queryMap.put("brand", brand.trim());

        if (filters.get("priceMin") != null && !filters.get("priceMin").toString().trim().isEmpty()) {
            queryMap.put("priceMin", new java.math.BigDecimal(filters.get("priceMin").toString()));
        }
        if (filters.get("priceMax") != null && !filters.get("priceMax").toString().trim().isEmpty()) {
            queryMap.put("priceMax", new java.math.BigDecimal(filters.get("priceMax").toString()));
        }
        if (filters.get("inventoryMin") != null && !filters.get("inventoryMin").toString().trim().isEmpty()) {
            queryMap.put("inventoryMin", Integer.valueOf(filters.get("inventoryMin").toString()));
        }

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
        return resultMap;
    }

    @Override
    public List<Map<String, Object>> getCategoryList() {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "select id, category_name from ys_category " +
                        "where status = 1 and parent_id = 0 " +
                        "order by sort_order asc"
        );
        return rows.stream().map(r -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", r.get("id"));
            m.put("name", r.get("category_name"));
            return m;
        }).collect(Collectors.toList());
    }

    @Override
    public List<Map<String, Object>> getSubCategories(Long parentId) {
        if (parentId == null || parentId <= 0) throw new IllegalArgumentException("parentId不能为空");
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "select id, category_name from ys_category " +
                        "where status = 1 and parent_id = ? " +
                        "order by sort_order asc",
                parentId
        );
        return rows.stream().map(r -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", r.get("id"));
            m.put("name", r.get("category_name"));
            return m;
        }).collect(Collectors.toList());
    }

    @Override
    public YsGoods getGoodsDetail(Long goodsId) {
        if (goodsId == null || goodsId <= 0) throw new IllegalArgumentException("商品ID不能为空");
        YsGoods goods = ysGoodsDao.selectGoodById(goodsId);
        if (goods == null) throw new IllegalStateException("商品不存在");
        log.info("获取商品详情成功: goodsId={}", goodsId);
        return goods;
    }

    @Override
    public Map<String, Object> getGoodsByCategory(Long categoryId, Integer pageNum, Integer pageSize) {
        if (pageNum == null || pageNum < 1) pageNum = 1;
        if (pageSize == null || pageSize < 1) pageSize = 10;
        Map<String, Object> queryMap = new HashMap<>();
        if (categoryId != null && categoryId > 0) queryMap.put("categoryId", categoryId);
        Page<YsGoods> page = new Page<>(pageNum, pageSize);
        IPage<YsGoods> resultPage = ysGoodsDao.queryAllGoodsPage(page, queryMap);
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("list", resultPage.getRecords());
        resultMap.put("total", resultPage.getTotal());
        resultMap.put("pageNum", resultPage.getCurrent());
        resultMap.put("pageSize", resultPage.getSize());
        resultMap.put("pages", resultPage.getPages());
        log.info("获取分类商品成功: categoryId={}, total={}", categoryId, resultPage.getTotal());
        return resultMap;
    }

    @Override
    public List<YsGoods> getHotGoods(Integer limit) {
        if (limit == null || limit < 1) limit = 10;
        if (limit > 100) limit = 100;
        Map<String, Object> queryMap = new HashMap<>();
        Page<YsGoods> page = new Page<>(1, limit);
        IPage<YsGoods> resultPage = ysGoodsDao.queryAllGoodsPage(page, queryMap);
        log.info("获取热门商品成功: limit={}", limit);
        return resultPage.getRecords();
    }
}
