package org.ys.transaction.application.conver;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;
import org.ys.transaction.domain.aggregate.UserAggregate;
import org.ys.transaction.domain.entity.YsUser;
import org.ys.transaction.domain.entity.YsUserAddr;

import java.util.List;

@Mapper
public interface UserConver {
    UserConver INSTANCE = Mappers.getMapper(UserConver.class);

    @Mappings({
            @Mapping(target = "user", source = "user"),
            @Mapping(target = "addresses", source = "addrs")
    })
    UserAggregate voToAggregate(YsUser user, List<YsUserAddr> addrs);
}
