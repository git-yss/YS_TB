package org.ys.transaction.application.conver;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import org.ys.transaction.Interface.VO.CategoryVO;

@Mapper
public interface CategoryConver {
    CategoryConver INSTANCE = Mappers.getMapper(CategoryConver.class);

    @Mapping(target = "name", source = "categoryName")
    CategoryVO toCategoryVO(org.ys.transaction.domain.entity.YsCategory category);
}
