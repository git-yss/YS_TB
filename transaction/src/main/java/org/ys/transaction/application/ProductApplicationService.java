package org.ys.transaction.application;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.ys.transaction.application.conver.CategoryConver;
import org.ys.transaction.application.conver.ProductConver;
import org.ys.transaction.Infrastructure.es.EsGoodsSearchService;
import org.ys.transaction.domain.aggregate.CateGoryAggregate;
import org.ys.transaction.domain.aggregate.GoodsAggregate;
import org.ys.transaction.domain.entity.YsCategory;
import org.ys.transaction.domain.entity.YsGoods;
import org.ys.transaction.domain.port.NaturalLanguageFilterParserPort;
import org.ys.transaction.domain.respository.YsCategoryRespository;
import org.ys.transaction.domain.respository.YsGoodsRespository;

import jakarta.annotation.Resource;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ProductApplicationService {

    @Resource
    private YsGoodsRespository ysGoodsRespository;

    @Resource
    private YsCategoryRespository ysCategoryRespository;

    @Resource
    private EsGoodsSearchService esGoodsSearchService;

    @Resource
    private NaturalLanguageFilterParserPort naturalLanguageFilterParserPort;

    public Map<String, Object> searchProducts(Map<String, Object> filters) {
        long startTime = System.currentTimeMillis();
        log.info("[商品搜索] 开始处理搜索请求, 原始参数: {}", filters);
        
        if (filters == null) filters = Collections.emptyMap();
        
        // 记录大模型查询耗时
        long aiStartTime = System.currentTimeMillis();
        Map<String, Object> mergedFilters = mergeAiFilters(filters);
        long aiEndTime = System.currentTimeMillis();
        long aiDuration = aiEndTime - aiStartTime;
        log.info("[商品搜索-大模型解析] 自然语言解析完成, 耗时: {}ms, 合并后参数: {}", aiDuration, mergedFilters);

        Integer pageNum = mergedFilters.get("pageNum") != null ? Integer.valueOf(mergedFilters.get("pageNum").toString()) : 1;
        Integer pageSize = mergedFilters.get("pageSize") != null ? Integer.valueOf(mergedFilters.get("pageSize").toString()) : 10;
        if (pageNum < 1) pageNum = 1;
        if (pageSize < 1) pageSize = 10;

        try {
            // 记录 ES 查询耗时
            long esStartTime = System.currentTimeMillis();
            Map<String, Object> result = esGoodsSearchService.search(mergedFilters, pageNum, pageSize);
            long esEndTime = System.currentTimeMillis();
            long esDuration = esEndTime - esStartTime;
            
            result.put("appliedFilters", mergedFilters);
            
            long totalDuration = System.currentTimeMillis() - startTime;
            log.info("[商品搜索-ES查询] ES查询完成, 耗时: {}ms, 总耗时: {}ms, 返回结果数: {}", 
                    esDuration, totalDuration, result.get("list") != null ? ((List<?>) result.get("list")).size() : 0);
            return result;
        } catch (Exception e) {
            long dbStartTime = System.currentTimeMillis();
            log.warn("[商品搜索-ES降级] ES查询失败, 降级到数据库查询, 错误: {}", e.getMessage());
            
            // Elasticsearch 不可用时降级到数据库查询，保证业务可用性
            Map<String, Object> result = searchProductsByDb(mergedFilters, pageNum, pageSize);
            long dbEndTime = System.currentTimeMillis();
            long dbDuration = dbEndTime - dbStartTime;
            
            result.put("appliedFilters", mergedFilters);
            
            long totalDuration = System.currentTimeMillis() - startTime;
            log.info("[商品搜索-DB查询] 数据库查询完成, 耗时: {}ms, 总耗时: {}ms, 返回结果数: {}", 
                    dbDuration, totalDuration, result.get("list") != null ? ((List<?>) result.get("list")).size() : 0);
            return result;
        }
    }

    private Map<String, Object> mergeAiFilters(Map<String, Object> filters) {
        Map<String, Object> merged = new HashMap<>();
        String nlQuery = filters.get("nlQuery") == null ? null : filters.get("nlQuery").toString();
        
        if (nlQuery != null && !nlQuery.trim().isEmpty()) {
            log.debug("[大模型解析] 检测到自然语言查询: {}", nlQuery);
        }
        
        Map<String, Object> aiFilters = naturalLanguageFilterParserPort.parseNaturalLanguage(nlQuery);
        if (aiFilters != null) {
            log.debug("[大模型解析] AI返回过滤条件: {}", aiFilters);
            merged.putAll(aiFilters);
        }
        merged.putAll(filters);
        return merged;
    }

    private Map<String, Object> searchProductsByDb(Map<String, Object> filters, Integer pageNum, Integer pageSize) {
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

    public Map<String, Object> rebuildEsGoodsIndex() {
        try {
            int count = esGoodsSearchService.rebuildGoodsIndex();
            Map<String, Object> result = new HashMap<>();
            result.put("indexedCount", count);
            return result;
        } catch (Exception e) {
            throw new IllegalStateException("重建ES索引失败: " + e.getMessage());
        }
    }

    public Map<String, Object> syncEsGoodsById(Long goodsId) {
        try {
            boolean ok = esGoodsSearchService.syncGoodsById(goodsId);
            Map<String, Object> result = new HashMap<>();
            result.put("goodsId", goodsId);
            result.put("synced", ok);
            return result;
        } catch (Exception e) {
            throw new IllegalStateException("同步商品到ES失败: " + e.getMessage());
        }
    }

    public List<String> esSuggest(String prefix, Integer size) {
        try {
            return esGoodsSearchService.suggest(prefix, size == null ? 10 : size);
        } catch (Exception e) {
            throw new IllegalStateException("ES联想词查询失败: " + e.getMessage());
        }
    }

    public List<Map<String, Object>> esHotKeywords(Integer size) {
        return esGoodsSearchService.hotKeywords(size == null ? 10 : size);
    }

    public Map<String, Object> esGoodsAliasStatus() {
        try {
            return esGoodsSearchService.aliasStatus();
        } catch (Exception e) {
            throw new IllegalStateException("获取 ES 别名状态失败: " + e.getMessage());
        }
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
