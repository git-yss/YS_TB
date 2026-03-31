package org.ys.transaction.domain.aggregate;

import lombok.Getter;
import org.ys.transaction.domain.entity.YsCategory;

@Getter
public class CateGoryAggregate {
    private final YsCategory ysCategory;

    public CateGoryAggregate(YsCategory ysCategory) {
        if (ysCategory == null) {
            throw new IllegalArgumentException("ysCategory cannot be null");
        }
        this.ysCategory = ysCategory;
    }
}
