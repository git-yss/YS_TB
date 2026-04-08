package org.ys.transaction.application;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;
import org.ys.transaction.domain.aggregate.OrderAggregate;

import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class AssistantToolService {
    private static final Logger log = LoggerFactory.getLogger(AssistantToolService.class);

    @Resource
    private OrderApplicationService orderApplicationService;

    @Resource
    private ProductApplicationService productApplicationService;

    @Tool(description = "查询用户最近订单列表。参数: userId(用户ID), limit(返回条数，建议1-5)")
    public Map<String, Object> getRecentOrders(Long userId, Integer limit) {
        log.info("assistant_tool getRecentOrders userId={} limit={}", userId, limit);
        Map<String, Object> result = new HashMap<>();
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
        return result;
    }

    @Tool(description = "查询订单支付状态。参数: userId(用户ID), orderId(订单ID)")
    public Map<String, Object> getOrderPaymentStatus(Long userId, Long orderId) {
        log.info("assistant_tool getOrderPaymentStatus userId={} orderId={}", userId, orderId);
        if (userId == null || userId <= 0 || orderId == null || orderId <= 0) {
            Map<String, Object> err = new HashMap<>();
            err.put("error", "参数不完整");
            return err;
        }
        Map<String, Object> detail = orderApplicationService.detail(orderId, userId);
        Map<String, Object> result = new HashMap<>();
        result.put("orderId", orderId);
        result.put("statusDesc", detail.get("statusDesc"));
        result.put("order", detail.get("order"));
        return result;
    }

    @Tool(description = "搜索商品或查询商品信息。参数: query(用户商品需求), pageSize(返回数量，建议1-5)")
    public Map<String, Object> searchProductsByQuery(String query, Integer pageSize) {
        log.info("assistant_tool searchProductsByQuery query={} pageSize={}", query, pageSize);
        Map<String, Object> filters = new HashMap<>();
        filters.put("nlQuery", query);
        filters.put("keyword", query);
        filters.put("pageNum", 1);
        filters.put("pageSize", pageSize == null ? 3 : Math.max(1, Math.min(pageSize, 5)));
        return productApplicationService.searchProducts(filters);
    }
}
