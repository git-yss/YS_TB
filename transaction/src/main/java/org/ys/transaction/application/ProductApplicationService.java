package org.ys.transaction.application;

import org.springframework.stereotype.Service;
import org.ys.transaction.application.conver.CategoryConver;
import org.ys.transaction.application.conver.ProductConver;
import org.ys.transaction.domain.aggregate.CateGoryAggregate;
import org.ys.transaction.domain.aggregate.GoodsAggregate;
import org.ys.transaction.domain.entity.YsCategory;
import org.ys.transaction.domain.entity.YsGoods;
import org.ys.transaction.domain.respository.YsCategoryRespository;
import org.ys.transaction.domain.respository.YsGoodsRespository;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ProductApplicationService {

    @Resource
    private YsGoodsRespository ysGoodsRespository;

    @Resource
    private YsCategoryRespository ysCategoryRespository;

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

        GoodsAggregate queryAggregate = new GoodsAggregate(
                null, null, null, new GoodsAggregate.Query(pageNum, pageSize, queryMap)
        );
        List<GoodsAggregate> goodsAggregates = ysGoodsRespository.queryAllGoodsPage(queryAggregate);
        List<org.ys.transaction.Infrastructure.pojo.YsGoods> goodsList = goodsAggregates.stream()
                .map(g -> ProductConver.INSTANCE.toGoodsPo(g.getGoods()))
                .collect(Collectors.toList());

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("list", goodsList);
        resultMap.put("total", goodsList.size());
        resultMap.put("pageNum", pageNum.longValue());
        resultMap.put("pageSize", pageSize.longValue());
        resultMap.put("pages", 1L);
        return resultMap;
    }

    public List<org.ys.transaction.Interface.VO.CategoryVO> getCategoryList() {
        List<CateGoryAggregate> aggregates = ysCategoryRespository.selectEnabled(
                new CateGoryAggregate(YsCategory.rehydrate(null, 0L, null, null, null, null, null, null, null, 1))
        );
        return aggregates.stream()
                .map(a -> CategoryConver.INSTANCE.toCategoryVO(a.getYsCategory()))
                .collect(Collectors.toList());
    }

    public List<org.ys.transaction.Interface.VO.CategoryVO> getSubCategories(Long parentId) {
        List<CateGoryAggregate> aggregates = ysCategoryRespository.selectByParentId(
                new CateGoryAggregate(YsCategory.rehydrate(null, parentId, null, null, null, null, null, null, null, null))
        );
        return aggregates.stream()
                .map(a -> CategoryConver.INSTANCE.toCategoryVO(a.getYsCategory()))
                .collect(Collectors.toList());
    }

    public org.ys.transaction.Infrastructure.pojo.YsGoods getGoodsDetail(Long goodsId) {
        GoodsAggregate goodsAggregate = ysGoodsRespository.selectGoodById(new GoodsAggregate(null, YsGoods.identify(goodsId), null));
        if (goodsAggregate == null || goodsAggregate.getGoods() == null) throw new IllegalStateException("商品不存在");
        return ProductConver.INSTANCE.toGoodsPo(goodsAggregate.getGoods());
    }

    public Map<String, Object> getGoodsByCategory(Long categoryId, Integer pageNum, Integer pageSize) {
        if (pageNum == null || pageNum < 1) pageNum = 1;
        if (pageSize == null || pageSize < 1) pageSize = 10;
        Map<String, Object> queryMap = new HashMap<>();
        if (categoryId != null && categoryId > 0) queryMap.put("categoryId", categoryId);
        GoodsAggregate queryAggregate = new GoodsAggregate(
                null, null, null, new GoodsAggregate.Query(pageNum, pageSize, queryMap)
        );
        List<GoodsAggregate> goodsAggregates = ysGoodsRespository.queryAllGoodsPage(queryAggregate);
        List<org.ys.transaction.Infrastructure.pojo.YsGoods> goodsList = goodsAggregates.stream()
                .map(g -> ProductConver.INSTANCE.toGoodsPo(g.getGoods()))
                .collect(Collectors.toList());
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("list", goodsList);
        resultMap.put("total", goodsList.size());
        resultMap.put("pageNum", pageNum.longValue());
        resultMap.put("pageSize", pageSize.longValue());
        resultMap.put("pages", 1L);
        return resultMap;
    }

    public List<org.ys.transaction.Infrastructure.pojo.YsGoods> getHotGoods(Integer limit) {
        if (limit == null || limit < 1) limit = 10;
        if (limit > 100) limit = 100;
        GoodsAggregate queryAggregate = new GoodsAggregate(
                null, null, null, new GoodsAggregate.Query(1, limit, new HashMap<>())
        );
        List<GoodsAggregate> goodsAggregates = ysGoodsRespository.queryAllGoodsPage(queryAggregate);
        return goodsAggregates.stream().map(g -> ProductConver.INSTANCE.toGoodsPo(g.getGoods())).collect(Collectors.toList());
    }
}
