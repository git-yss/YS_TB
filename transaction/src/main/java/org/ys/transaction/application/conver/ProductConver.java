package org.ys.transaction.application.conver;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface ProductConver {
    ProductConver INSTANCE = Mappers.getMapper(ProductConver.class);

    org.ys.transaction.Infrastructure.pojo.YsGoods toGoodsPo(org.ys.transaction.domain.entity.YsGoods goods);
}
