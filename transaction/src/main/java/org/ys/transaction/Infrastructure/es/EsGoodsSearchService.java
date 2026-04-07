package org.ys.transaction.Infrastructure.es;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.Request;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.client.ResponseException;
import org.elasticsearch.client.Response;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.suggest.Suggest;
import org.elasticsearch.search.suggest.SuggestBuilder;
import org.elasticsearch.search.suggest.SuggestBuilders;
import org.elasticsearch.search.suggest.completion.CompletionSuggestion;
import org.elasticsearch.search.sort.SortOrder;
import org.elasticsearch.xcontent.XContentType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.ys.transaction.Infrastructure.dao.YsGoodsDao;
import org.ys.transaction.Infrastructure.pojo.YsGoods;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class EsGoodsSearchService {

    @Resource
    private RestHighLevelClient esClient;

    @Resource
    private YsGoodsDao ysGoodsDao;

    @Resource
    private ObjectMapper objectMapper;

    @Value("${app.es.goods-index-alias:ys_goods_alias}")
    private String goodsIndexAlias;

    @Value("${app.es.goods-index-prefix:ys_goods_index_v}")
    private String goodsIndexPrefix;

    private final ConcurrentHashMap<String, AtomicLong> hotKeywords = new ConcurrentHashMap<>();

    /**
     * 搜索主流程：
     * 1) 确保别名可用（首次会初始化 v1 索引并绑定别名）
     * 2) 组装 bool 查询（全文 + 过滤 + 排序 + 分页）
     * 3) 返回业务结构，供现有前端直接消费
     */
    public Map<String, Object> search(Map<String, Object> filters, int pageNum, int pageSize) throws IOException {
        // 确保 ES 索引已存在，如果不存在则创建初始索引
        ensureIndexExists();

        // 计算分页偏移量：ES 的 from 参数表示从第几条开始（从0开始计数）
        int from = (pageNum - 1) * pageSize;
        // 创建 SearchSourceBuilder，用于构建查询请求体
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        // 设置分页参数：from 起始位置，size 每页数量
        sourceBuilder.from(from).size(pageSize);

        // 创建布尔查询对象，支持 must(必须匹配)、filter(过滤)、should(应该匹配)、must_not(必须不匹配)
        BoolQueryBuilder bool = QueryBuilders.boolQuery();

        // 从过滤器中提取关键词
        String keyword = value(filters, "keyword");
        // 如果关键词不为空，进行全文检索
        if (keyword != null && !keyword.trim().isEmpty()) {
            // 记录热词，用于热门搜索统计
            recordKeyword(keyword.trim());
            // 使用 multiMatchQuery 在多个字段中搜索关键词
            // field() 方法设置不同字段的权重（boost），name 权重最高 4.0，brand 次之 3.0，introduce 最低 2.0
            bool.must(QueryBuilders.multiMatchQuery(keyword.trim(), "name", "introduce", "brand", "category")
                    .field("name", 4.0f)
                    .field("brand", 3.0f)
                    .field("introduce", 2.0f));

            // 创建高亮构建器，用于在搜索结果中高亮显示匹配的关键词
            HighlightBuilder highlightBuilder = new HighlightBuilder();
            // 配置 name 字段的高亮标签为 <em></em>
            highlightBuilder.field("name").preTags("<em>").postTags("</em>");
            // 配置 introduce 字段的高亮标签为 <em></em>
            highlightBuilder.field("introduce").preTags("<em>").postTags("</em>");
            // 将高亮配置添加到查询中
            sourceBuilder.highlighter(highlightBuilder);
        }

        // 从过滤器中提取分类 ID
        Long categoryId = longValue(filters, "categoryId");
        // 如果分类 ID 有效，添加分类过滤条件（term 精确匹配）
        if (categoryId != null && categoryId > 0) {
            bool.filter(QueryBuilders.termQuery("categoryId", categoryId));
        }

        // 从过滤器中提取品牌名称
        String brand = value(filters, "brand");
        // 如果品牌不为空，使用 matchPhraseQuery 进行短语匹配（保持词序）
        if (brand != null && !brand.trim().isEmpty()) {
            bool.filter(QueryBuilders.matchPhraseQuery("brand", brand.trim()));
        }

        // 从过滤器中提取价格范围的最小值和最大值
        String priceMin = value(filters, "priceMin");
        String priceMax = value(filters, "priceMax");
        // 如果设置了价格范围，添加范围过滤条件
        if (priceMin != null || priceMax != null) {
            // 同时设置了最小和最大价格
            if (priceMin != null && !priceMin.trim().isEmpty() && priceMax != null && !priceMax.trim().isEmpty()) {
                bool.filter(QueryBuilders.rangeQuery("price").gte(priceMin).lte(priceMax));
            } else if (priceMin != null && !priceMin.trim().isEmpty()) {
                // 只设置了最小价格
                bool.filter(QueryBuilders.rangeQuery("price").gte(priceMin));
            } else if (priceMax != null && !priceMax.trim().isEmpty()) {
                // 只设置了最大价格
                bool.filter(QueryBuilders.rangeQuery("price").lte(priceMax));
            }
        }

        // 从过滤器中提取库存最小值
        String inventoryMin = value(filters, "inventoryMin");
        // 如果设置了库存最小值，添加库存范围过滤
        if (inventoryMin != null && !inventoryMin.trim().isEmpty()) {
            bool.filter(QueryBuilders.rangeQuery("inventory").gte(inventoryMin));
        }

        // 将布尔查询设置到查询源中
        sourceBuilder.query(bool);
        // 应用排序规则
        applySort(sourceBuilder, value(filters, "sort"));

        // 创建搜索请求，指定查询的索引（通过别名访问）
        SearchRequest request = new SearchRequest(goodsIndexAlias).source(sourceBuilder);
        // 执行搜索请求，获取响应结果
        SearchResponse response = esClient.search(request, RequestOptions.DEFAULT);

        // 创建结果列表，用于存储转换后的商品对象
        List<YsGoods> list = new ArrayList<>();
        // 遍历所有命中的文档
        for (SearchHit hit : response.getHits().getHits()) {
            // 将 ES 返回的 SourceAsMap 转换为 YsGoods 对象
            YsGoods g = objectMapper.convertValue(hit.getSourceAsMap(), YsGoods.class);
            // 如果存在高亮字段且包含 name 字段，使用高亮后的文本替换原始名称
            if (hit.getHighlightFields() != null && hit.getHighlightFields().containsKey("name")) {
                // 获取高亮片段数组的第一个元素，并设置为商品名称
                g.setName(hit.getHighlightFields().get("name").fragments()[0].string());
            }
            // 将处理后的商品对象添加到列表中
            list.add(g);
        }

        // 创建返回结果 Map
        Map<String, Object> result = new HashMap<>();
        // 放入商品列表
        result.put("list", list);
        // 放入总记录数（如果 getTotalHits() 为 null，则默认为 0）
        result.put("total", response.getHits().getTotalHits() == null ? 0 : response.getHits().getTotalHits().value);
        // 放入当前页码
        result.put("pageNum", (long) pageNum);
        // 放入每页大小
        result.put("pageSize", (long) pageSize);
        // 计算总页数：向上取整，至少为 1 页
        result.put("pages", Math.max(1L, (long) Math.ceil(((double) ((Number) result.get("total")).longValue()) / pageSize)));
        // 返回封装好的搜索结果
        return result;
    }

    /**
     * 重建商品索引：采用零停机切换策略
     * 1) 创建新版本索引
     * 2) 全量导入数据到新索引
     * 3) 原子切换别名指向新索引
     * 4) 旧索引可后续清理
     */
    public int rebuildGoodsIndex() throws IOException {
        // 确保至少有一个索引存在（首次初始化）
        ensureIndexExists();

        // 获取当前别名指向的活跃索引名称
        String oldIndex = getActiveIndex();
        // 生成新的索引名称：前缀 + 时间戳，保证唯一性
        String newIndex = goodsIndexPrefix + System.currentTimeMillis();
        // 创建新索引并设置映射（mappings）
        createIndexWithMapping(newIndex);

        // 从数据库查询所有商品数据
        List<YsGoods> goodsList = ysGoodsDao.selectList(null);
        // 如果没有商品数据，直接返回 0
        if (goodsList == null || goodsList.isEmpty()) {
            return 0;
        }

        // ES 8 + 7.x HighLevelClient 在 /_bulk 响应解析上可能不兼容；
        // 这里使用 low-level NDJSON 直接写入，避免高层 BulkResponse 反序列化问题。
        // 构建 NDJSON 格式的批量请求字符串
        StringBuilder ndjson = new StringBuilder();
        // 遍历所有商品，构建批量索引请求
        for (YsGoods goods : goodsList) {
            // 将商品对象转换为 ES 文档格式
            Map<String, Object> source = toEsSource(goods);
            // 添加 action 行：指定索引操作、索引名称、文档 ID
            ndjson.append("{\"index\":{\"_index\":\"").append(newIndex).append("\",\"_id\":\"").append(goods.getId()).append("\"}}\n");
            // 添加 source 行：文档的实际内容（JSON 格式）
            ndjson.append(objectMapper.writeValueAsString(source)).append("\n");
        }

        // 创建低级别批量请求，使用 POST /_bulk 端点
        Request bulkRequest = new Request("POST", "/_bulk");
        // 使用 setJsonEntity 发送 NDJSON 数据，避免不同版本 RestClient Request 对 setEntity/headers 的差异
        bulkRequest.setJsonEntity(ndjson.toString());
        // 通过低级别客户端执行批量请求（高性能）
        esClient.getLowLevelClient().performRequest(bulkRequest);

        // 原子切换别名：先指向新索引，再移除旧索引的别名绑定
        // 这个操作是原子的，保证查询不会中断
        switchAliasAtomic(oldIndex, newIndex);
        // 返回成功导入的商品数量
        return goodsList.size();
    }

    /**
     * 增量同步单个商品到 ES
     * 用于商品创建、更新时的实时同步
     */
    public boolean syncGoodsById(Long goodsId) throws IOException {
        // 校验商品 ID 的有效性
        if (goodsId == null || goodsId <= 0) {
            throw new IllegalArgumentException("goodsId不能为空");
        }
        // 确保索引存在
        ensureIndexExists();
        // 获取当前别名指向的活跃索引名称
        String activeIndex = getActiveIndex();
        // 从数据库查询商品详情
        YsGoods goods = ysGoodsDao.selectById(goodsId);
        // 如果商品不存在，返回 false
        if (goods == null) {
            return false;
        }
        // 将商品对象转换为 ES 文档格式
        Map<String, Object> source = toEsSource(goods);
        // 创建索引请求：指定索引名称、文档 ID、文档内容
        IndexRequest request = new IndexRequest(activeIndex != null ? activeIndex : goodsIndexAlias)
                .id(String.valueOf(goods.getId()))
                .source(source, XContentType.JSON);
        // 执行索引操作（如果文档已存在则覆盖更新）
        esClient.index(request, RequestOptions.DEFAULT);
        // 返回同步成功
        return true;
    }

    /**
     * 从 ES 中删除商品文档
     * 用于商品下架或删除时的同步
     */
    public boolean deleteGoodsById(Long goodsId) throws IOException {
        // 校验商品 ID 的有效性
        if (goodsId == null || goodsId <= 0) {
            throw new IllegalArgumentException("goodsId不能为空");
        }
        // 确保索引存在
        ensureIndexExists();
        // 获取当前别名指向的活跃索引名称
        String activeIndex = getActiveIndex();
        // 如果没有活跃索引，返回 false
        if (activeIndex == null) return false;

        // 创建删除请求：DELETE /{index}/_doc/{id}
        Request request = new Request("DELETE", "/" + activeIndex + "/_doc/" + goodsId);
        try {
            // 执行删除请求
            esClient.getLowLevelClient().performRequest(request);
            // 删除成功，返回 true
            return true;
        } catch (ResponseException e) {
            // 捕获异常，判断是否为 404（文档不存在）
            if (e.getResponse() != null && e.getResponse().getStatusLine() != null
                    && e.getResponse().getStatusLine().getStatusCode() == 404) {
                // 文档不存在，返回 false（不算错误）
                return false;
            }
            // 其他异常继续抛出
            throw e;
        }
    }

    /**
     * 搜索建议（自动补全）
     * 基于 completion suggester 实现前缀匹配
     */
    public List<String> suggest(String prefix, int size) throws IOException {
        // 确保索引存在
        ensureIndexExists();
        // 如果前缀为空，返回空列表
        if (prefix == null || prefix.trim().isEmpty()) {
            return Collections.emptyList();
        }
        // 限制建议数量范围：最小 1，最大 20
        if (size <= 0) size = 10;
        if (size > 20) size = 20;

        // 创建查询源构建器
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        // 创建搜索建议构建器
        SuggestBuilder suggestBuilder = new SuggestBuilder();
        // 添加 completion 类型的建议器
        suggestBuilder.addSuggestion(
                // 建议器名称
                "goods_suggest",
                // 使用 completionSuggestion，针对 suggest 字段进行前缀匹配
                SuggestBuilders.completionSuggestion("suggest")
                        .prefix(prefix.trim())  // 设置前缀
                        .skipDuplicates(true)   // 跳过重复建议
                        .size(size)             // 返回建议数量
        );
        // 将建议器添加到查询源中
        sourceBuilder.suggest(suggestBuilder);
        // 不需要返回实际文档，只返回建议结果，所以设置 size=0
        sourceBuilder.size(0);

        // 创建搜索请求，指定索引别名
        SearchRequest request = new SearchRequest(goodsIndexAlias).source(sourceBuilder);
        // 执行搜索请求
        SearchResponse response = esClient.search(request, RequestOptions.DEFAULT);
        // 获取建议结果
        Suggest suggest = response.getSuggest();
        // 如果没有建议结果，返回空列表
        if (suggest == null) return Collections.emptyList();

        // 获取名为 "goods_suggest" 的 completion 建议
        CompletionSuggestion suggestion = suggest.getSuggestion("goods_suggest");
        // 如果建议不存在，返回空列表
        if (suggestion == null) return Collections.emptyList();
        // 创建结果列表
        List<String> result = new ArrayList<>();
        // 遍历所有建议选项
        for (CompletionSuggestion.Entry.Option option : suggestion.getOptions()) {
            // 获取建议文本
            Text text = option.getText();
            // 如果文本不为空，添加到结果列表
            if (text != null) {
                result.add(text.string());
            }
        }
        // 返回建议列表
        return result;
    }

    /**
     * 获取热门搜索关键词列表
     * 基于内存中的 ConcurrentHashMap 统计
     */
    public List<Map<String, Object>> hotKeywords(int size) {
        // 限制返回数量范围：最小 1，最大 50
        if (size <= 0) size = 10;
        if (size > 50) size = 50;
        // 将 ConcurrentHashMap 的 entrySet 转换为可排序的 ArrayList
        List<Map.Entry<String, AtomicLong>> entries = new ArrayList<>(hotKeywords.entrySet());
        // 按搜索次数降序排序（使用 Comparator 比较 AtomicLong 的值）
        entries.sort(Comparator.comparingLong((Map.Entry<String, AtomicLong> e) -> e.getValue().get()).reversed());
        // 创建结果列表
        List<Map<String, Object>> result = new ArrayList<>();
        // 遍历排序后的条目，取前 size 个
        for (int i = 0; i < Math.min(size, entries.size()); i++) {
            // 创建每个热词的 Map 对象
            Map<String, Object> row = new HashMap<>();
            // 放入关键词
            row.put("keyword", entries.get(i).getKey());
            // 放入搜索次数
            row.put("count", entries.get(i).getValue().get());
            // 添加到结果列表
            result.add(row);
        }
        // 返回热词列表
        return result;
    }

    /**
     * 确保 ES 索引存在
     * 如果是首次调用，会创建 v1 索引并绑定别名
     */
    private void ensureIndexExists() throws IOException {
        // 检查是否已有活跃索引（通过别名查询）
        String activeIndex = getActiveIndex();
        // 如果已有活跃索引，直接返回，无需初始化
        if (activeIndex != null) return;

        // 第一次初始化：创建 v1 物理索引并绑定别名
        String firstIndex = goodsIndexPrefix + "1";
        // 创建索引并设置 mappings
        createIndexWithMapping(firstIndex);
        // 将别名绑定到新创建的索引
        switchAliasAtomic(null, firstIndex);
    }

    /**
     * 获取当前别名指向的目标索引名称
     */
    public String getActiveIndexAliasTarget() throws IOException {
        return getActiveIndex();
    }

    /**
     * 获取别名状态信息
     * 用于监控和管理索引版本
     */
    public Map<String, Object> aliasStatus() throws IOException {
        // 创建结果 Map
        Map<String, Object> result = new HashMap<>();
        // 放入别名名称
        result.put("alias", goodsIndexAlias);
        // 放入当前活跃索引名称
        result.put("activeIndex", getActiveIndex());
        // 返回状态信息
        return result;
    }

    /**
     * 获取当前别名指向的活跃索引名称
     * 通过查询 ES 的 _alias API 获取
     */
    private String getActiveIndex() throws IOException {
        // 查询别名映射：/_alias/{alias} -> { indexName: { aliases: { aliasName: {} } } }
        try {
            // 创建 GET 请求，查询别名的映射关系
            Request request = new Request("GET", "/_alias/" + goodsIndexAlias);
            // 通过低级别客户端执行请求
            Response response = esClient.getLowLevelClient().performRequest(request);
            // 获取响应体内容，如果为空则返回 null
            String body = response.getEntity() == null ? null : EntityUtils.toString(response.getEntity(), "UTF-8");
            if (body == null || body.trim().isEmpty()) return null;

            // 将 JSON 响应解析为 Map 结构
            Map<String, Object> parsed = objectMapper.readValue(body, new TypeReference<Map<String, Object>>() {});
            // 如果解析结果为空，返回 null
            if (parsed == null || parsed.isEmpty()) return null;
            // 返回第一个键名（即索引名称），因为一个别名只能指向一个索引
            return parsed.keySet().iterator().next();
        } catch (ResponseException e) {
            // 捕获响应异常
            if (e.getResponse() != null && e.getResponse().getStatusLine() != null) {
                int status = e.getResponse().getStatusLine().getStatusCode();
                // 如果是 404，说明别名不存在，返回 null
                if (status == 404) return null;
            }
            // 其他异常继续抛出
            throw e;
        }
    }

    /**
     * 原子切换别名指向
     * 使用 ES 的 _aliases API 在一次请求中完成 add 和 remove 操作，保证原子性
     */
    private void switchAliasAtomic(String oldIndex, String newIndex) throws IOException {
        // /_aliases 支持 add/remove 在一次请求里完成，切换过程原子可见
        // 构建 JSON 请求体
        StringBuilder sb = new StringBuilder();
        sb.append("{\"actions\":");
        // 添加新索引的别名绑定操作
        sb.append("[{\"add\":{\"index\":\"").append(newIndex).append("\",\"alias\":\"").append(goodsIndexAlias).append("\"}}");
        // 如果有旧索引且与新索引不同，添加移除旧索引别名绑定的操作
        if (oldIndex != null && !oldIndex.trim().isEmpty() && !oldIndex.equals(newIndex)) {
            sb.append(",{\"remove\":{\"index\":\"").append(oldIndex).append("\",\"alias\":\"").append(goodsIndexAlias).append("\"}}");
        }
        sb.append("]}");

        // 创建 POST /_aliases 请求
        Request request = new Request("POST", "/_aliases");
        // 设置请求体为构建好的 JSON
        request.setJsonEntity(sb.toString());
        // 执行别名切换请求
        esClient.getLowLevelClient().performRequest(request);
    }

    /**
     * 创建 ES 索引并设置映射（mappings）
     * 定义字段类型、分词器等
     */
    private void createIndexWithMapping(String indexName) throws IOException {
        // 创建索引请求对象
        CreateIndexRequest createIndexRequest = new CreateIndexRequest(indexName);
        // 设置索引的配置和映射，使用 JSON 字符串格式
        createIndexRequest.source("{\n" +
                "  \"settings\": {\n" +
                "    \"number_of_shards\": 1,\n" +  // 主分片数量为 1
                "    \"number_of_replicas\": 0\n" +  // 副本数量为 0（单节点环境）
                "  },\n" +
                "  \"mappings\": {\n" +
                "    \"properties\": {\n" +
                "      \"id\": {\"type\": \"long\"},\n" +  // 商品 ID，长整型
                "      \"name\": {\"type\": \"text\"},\n" +  // 商品名称，文本类型（会分词）
                "      \"introduce\": {\"type\": \"text\"},\n" +  // 商品介绍，文本类型
                "      \"brand\": {\"type\": \"text\", \"fields\": {\"keyword\": {\"type\": \"keyword\"}}},\n" +  // 品牌，多字段映射：text 用于搜索，keyword 用于聚合
                "      \"category\": {\"type\": \"text\", \"fields\": {\"keyword\": {\"type\": \"keyword\"}}},\n" +  // 分类，多字段映射
                "      \"categoryId\": {\"type\": \"long\"},\n" +  // 分类 ID，长整型
                "      \"price\": {\"type\": \"double\"},\n" +  // 价格，双精度浮点数
                "      \"inventory\": {\"type\": \"integer\"},\n" +  // 库存，整数类型
                "      \"image\": {\"type\": \"keyword\"},\n" +  // 图片 URL，关键词类型（不分词）
                "      \"suggest\": {\"type\": \"completion\"}\n" +  // 搜索建议字段，completion 类型用于自动补全
                "    }\n" +
                "  }\n" +
                "}", XContentType.JSON);
        // 执行创建索引请求
        esClient.indices().create(createIndexRequest, RequestOptions.DEFAULT);
    }

    /**
     * 将 YsGoods 对象转换为 ES 文档格式
     * 添加 suggest 字段用于自动补全
     */
    private Map<String, Object> toEsSource(YsGoods goods) {
        // 使用 ObjectMapper 将商品对象转换为 Map（ES 文档的 source）
        Map<String, Object> source = objectMapper.convertValue(goods, new TypeReference<Map<String, Object>>() {});
        // 创建搜索建议输入列表
        List<String> suggestInputs = new ArrayList<>();
        // 如果商品名称不为空，添加到建议输入列表
        if (goods.getName() != null && !goods.getName().trim().isEmpty()) suggestInputs.add(goods.getName().trim());
        // 如果品牌不为空，添加到建议输入列表
        if (goods.getBrand() != null && !goods.getBrand().trim().isEmpty()) suggestInputs.add(goods.getBrand().trim());
        // 如果分类不为空，添加到建议输入列表
        if (goods.getCategory() != null && !goods.getCategory().trim().isEmpty()) suggestInputs.add(goods.getCategory().trim());
        // 将建议输入列表包装为 ES completion 类型要求的格式：{"input": [...]}
        source.put("suggest", Collections.singletonMap("input", suggestInputs));
        // 返回转换后的文档 Map
        return source;
    }

    /**
     * 记录搜索关键词，用于热词统计
     * 使用线程安全的 ConcurrentHashMap 和 AtomicLong
     */
    private void recordKeyword(String keyword) {
        // 如果关键词为空，直接返回
        if (keyword == null || keyword.trim().isEmpty()) return;
        // computeIfAbsent：如果 key 不存在则创建新的 AtomicLong(0)，然后递增计数
        // 这种方式保证了线程安全和高性能
        hotKeywords.computeIfAbsent(keyword, k -> new AtomicLong(0)).incrementAndGet();
    }

    /**
     * 应用排序规则到查询中
     * 支持多种排序方式：最新、价格升降序、库存升降序
     */
    private static void applySort(SearchSourceBuilder sourceBuilder, String sort) {
        // 如果没有指定排序规则，默认按 ID 降序（最新的在前）
        if (sort == null) {
            sourceBuilder.sort("id", SortOrder.DESC);
            return;
        }
        // 根据不同的排序参数设置排序规则
        switch (sort) {
            case "priceAsc":
                // 价格升序（从低到高）
                sourceBuilder.sort("price", SortOrder.ASC);
                break;
            case "priceDesc":
                // 价格降序（从高到低）
                sourceBuilder.sort("price", SortOrder.DESC);
                break;
            case "inventoryAsc":
                // 库存升序
                sourceBuilder.sort("inventory", SortOrder.ASC);
                break;
            case "inventoryDesc":
                // 库存降序
                sourceBuilder.sort("inventory", SortOrder.DESC);
                break;
            case "newest":
            default:
                // 默认排序：按 ID 降序（最新商品在前）
                sourceBuilder.sort("id", SortOrder.DESC);
                break;
        }
    }

    /**
     * 从 Map 中安全地提取字符串值
     * 处理 null 值的情况
     */
    private static String value(Map<String, Object> map, String key) {
        // 从 Map 中获取指定 key 的值
        Object v = map.get(key);
        // 如果值为 null，返回 null；否则转换为字符串
        return v == null ? null : v.toString();
    }

    /**
     * 从 Map 中安全地提取 Long 类型值
     * 处理类型转换异常
     */
    private static Long longValue(Map<String, Object> map, String key) {
        // 从 Map 中获取指定 key 的值
        Object v = map.get(key);
        // 如果值为 null，返回 null
        if (v == null) return null;
        try {
            // 尝试将值转换为 Long 类型
            return Long.valueOf(v.toString());
        } catch (Exception ignore) {
            // 如果转换失败（如格式错误），返回 null
            return null;
        }
    }
}
