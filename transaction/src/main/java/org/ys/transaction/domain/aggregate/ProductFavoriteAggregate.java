package org.ys.transaction.domain.aggregate;

import lombok.Getter;
import org.ys.transaction.domain.entity.YsProductFavorite;

@Getter
public class ProductFavoriteAggregate {
    private final YsProductFavorite favorite;

    public ProductFavoriteAggregate(YsProductFavorite favorite) {
        this.favorite = favorite;
    }
}
