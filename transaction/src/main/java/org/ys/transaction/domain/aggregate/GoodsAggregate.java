package org.ys.transaction.domain.aggregate;

import lombok.Getter;
import org.ys.transaction.domain.entity.YsGoods;
import org.ys.transaction.domain.entity.YsOrder;
import org.ys.transaction.domain.entity.YsUser;

import java.util.Collections;
import java.util.Map;

@Getter
public class GoodsAggregate {
    @Getter
    public static class Query {
        private final long pageNo;
        private final long pageSize;
        private final Map<String, Object> filters;

        public Query(long pageNo, long pageSize, Map<String, Object> filters) {
            this.pageNo = pageNo <= 0 ? 1 : pageNo;
            this.pageSize = pageSize <= 0 ? 10 : pageSize;
            this.filters = filters == null ? Collections.emptyMap() : Collections.unmodifiableMap(filters);
        }
    }

    private final YsOrder order;
    private final YsGoods goods;
    private final YsUser user;
    private final Query query;

    public GoodsAggregate(YsOrder order, YsGoods goods, YsUser user) {
        this(order, goods, user, null);
    }

    public GoodsAggregate(YsOrder order, YsGoods goods, YsUser user, Query query) {
        this.order = order;
        this.goods = goods;
        this.user = user;
        this.query = query;
    }
}
