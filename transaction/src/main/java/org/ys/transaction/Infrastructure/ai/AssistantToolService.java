package org.ys.transaction.Infrastructure.ai;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.ys.transaction.application.OrderApplicationService;
import org.ys.transaction.application.ProductApplicationService;
import org.ys.transaction.application.RagKnowledgeApplicationService;
import org.ys.transaction.domain.aggregate.OrderAggregate;

import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class AssistantToolService {
    private static final Logger log = LoggerFactory.getLogger(AssistantToolService.class);
    private static final ThreadLocal<List<Map<String, Object>>> TOOL_TRACE_HOLDER = new ThreadLocal<List<Map<String, Object>>>();

    @Resource
    private OrderApplicationService orderApplicationService;

    @Resource
    private ProductApplicationService productApplicationService;

    @Autowired(required = false)
    private RagKnowledgeApplicationService ragKnowledgeApplicationService;

    @Tool(description = "查询用户最近订单列表。参数: userId(用户ID), limit(返回条数，建议1-5)")
    public Map<String, Object> getRecentOrders(Long userId, Integer limit) {
        log.info("assistant_tool getRecentOrders userId={} limit={}", userId, limit);
        Map<String, Object> trace = beginTrace("getRecentOrders", buildArgs("userId", userId, "limit", limit));
        Map<String, Object> result = new HashMap<>();
        try {
            if (userId == null || userId <= 0) {
                result.put("error", "用户未登录");
                return result;
            }
            int size = limit == null ? 3 : Math.max(1, Math.min(limit, 5));
            List<OrderAggregate> list = orderApplicationService.list(userId);
            list.sort(new Comparator<OrderAggregate>() {
                @Override
                public int compare(OrderAggregate a, OrderAggregate b) {
                    java.time.LocalDateTime t1 = a.getOrder() == null ? null : a.getOrder().getAddTime();
                    java.time.LocalDateTime t2 = b.getOrder() == null ? null : b.getOrder().getAddTime();
                    if (t1 == null && t2 == null) return 0;
                    if (t1 == null) return 1;
                    if (t2 == null) return -1;
                    return t2.compareTo(t1);
                }
            });
            List<Map<String, Object>> rows = new ArrayList<>();
            for (int i = 0; i < Math.min(size, list.size()); i++) {
                OrderAggregate oa = list.get(i);
                Map<String, Object> row = new HashMap<>();
                row.put("orderId", oa.getOrder().getId());
                row.put("status", oa.getStatusDesc());
                row.put("totalAmount", oa.getOrder().getTotalAmount());
                row.put("addTime", oa.getOrder().getAddTime());
                rows.add(row);
            }
            result.put("orders", rows);
            finishTrace(trace, "ok", null);
            return result;
        } catch (Exception ex) {
            finishTrace(trace, "error", ex.getMessage());
            throw ex;
        }
    }

    @Tool(description = "查询订单支付状态。参数: userId(用户ID), orderId(订单ID)")
    public Map<String, Object> getOrderPaymentStatus(Long userId, Long orderId) {
        log.info("assistant_tool getOrderPaymentStatus userId={} orderId={}", userId, orderId);
        Map<String, Object> trace = beginTrace("getOrderPaymentStatus", buildArgs("userId", userId, "orderId", orderId));
        try {
            if (userId == null || userId <= 0 || orderId == null || orderId <= 0) {
                Map<String, Object> err = new HashMap<>();
                err.put("error", "参数不完整");
                finishTrace(trace, "ok", null);
                return err;
            }
            Map<String, Object> detail = orderApplicationService.detail(orderId, userId);
            Map<String, Object> result = new HashMap<>();
            result.put("orderId", orderId);
            result.put("statusDesc", detail.get("statusDesc"));
            result.put("order", detail.get("order"));
            finishTrace(trace, "ok", null);
            return result;
        } catch (Exception ex) {
            finishTrace(trace, "error", ex.getMessage());
            throw ex;
        }
    }

    @Tool(description = "搜索商品或查询商品信息。参数: query(用户商品需求), pageSize(返回数量，建议1-5)")
    public Map<String, Object> searchProductsByQuery(String query, Integer pageSize) {
        log.info("assistant_tool searchProductsByQuery query={} pageSize={}", query, pageSize);
        Map<String, Object> trace = beginTrace("searchProductsByQuery", buildArgs("query", query, "pageSize", pageSize));
        try {
            Map<String, Object> filters = new HashMap<>();
            filters.put("nlQuery", query);
            filters.put("keyword", query);
            filters.put("pageNum", 1);
            filters.put("pageSize", pageSize == null ? 3 : Math.max(1, Math.min(pageSize, 5)));
            Map<String, Object> data = productApplicationService.searchProducts(filters);
            finishTrace(trace, "ok", null);
            return data;
        } catch (Exception ex) {
            finishTrace(trace, "error", ex.getMessage());
            throw ex;
        }
    }

    @Tool(description = "检索商城知识库（退换货规则、活动说明、常见问题等）。参数: query(检索关键词或用户问题)")
    public Map<String, Object> searchKnowledgeBase(String query) {
        log.info("assistant_tool searchKnowledgeBase query={}", query);
        Map<String, Object> traceArgs = new HashMap<>();
        traceArgs.put("query", query);
        Map<String, Object> trace = beginTrace("searchKnowledgeBase", traceArgs);
        Map<String, Object> result = new HashMap<>();
        try {
            if (ragKnowledgeApplicationService == null) {
                result.put("found", false);
                result.put("snippets", "");
                result.put("message", "知识库未启用(app.rag.enabled=false 或未装配)");
                finishTrace(trace, "ok", null);
                return result;
            }
            if (query == null || query.trim().isEmpty()) {
                result.put("found", false);
                result.put("snippets", "");
                finishTrace(trace, "ok", null);
                return result;
            }
            Map<String, Object> rag = ragKnowledgeApplicationService.retrieveForAgent(query.trim());
            finishTrace(trace, "ok", null);
            return rag;
        } catch (Exception ex) {
            finishTrace(trace, "error", ex.getMessage());
            throw ex;
        }
    }

    public void resetTrace() {
        TOOL_TRACE_HOLDER.set(new CopyOnWriteArrayList<Map<String, Object>>());
    }

    public List<Map<String, Object>> getAndClearTrace() {
        List<Map<String, Object>> list = TOOL_TRACE_HOLDER.get();
        TOOL_TRACE_HOLDER.remove();
        return list == null ? new ArrayList<Map<String, Object>>() : list;
    }

    private static Map<String, Object> beginTrace(String toolName, Map<String, Object> args) {
        List<Map<String, Object>> list = TOOL_TRACE_HOLDER.get();
        if (list == null) {
            list = new CopyOnWriteArrayList<Map<String, Object>>();
            TOOL_TRACE_HOLDER.set(list);
        }
        Map<String, Object> row = new HashMap<String, Object>();
        row.put("tool", toolName);
        row.put("args", args);
        row.put("startedAt", System.currentTimeMillis());
        row.put("status", "running");
        list.add(row);
        return row;
    }

    private static void finishTrace(Map<String, Object> trace, String status, String error) {
        if (trace == null) return;
        long endedAt = System.currentTimeMillis();
        Object started = trace.get("startedAt");
        long startedAt = started instanceof Number ? ((Number) started).longValue() : endedAt;
        trace.put("endedAt", endedAt);
        trace.put("durationMs", Math.max(0L, endedAt - startedAt));
        trace.put("status", status);
        if (error != null && !error.trim().isEmpty()) {
            trace.put("error", error);
        }
    }

    private static Map<String, Object> buildArgs(String k1, Object v1, String k2, Object v2) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put(k1, v1);
        map.put(k2, v2);
        return map;
    }
}