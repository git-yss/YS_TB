package org.ys.transaction.Infrastructure.conver;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import org.ys.transaction.Infrastructure.pojo.YsUser;
import org.ys.transaction.domain.entity.YsUserAddr;
import org.ys.transaction.domain.aggregate.UserAggregate;

import java.util.List;

@Mapper
public interface UserConver {
    UserConver INSTANCE = Mappers.getMapper(UserConver.class);

    default UserAggregate poToAggregate(YsUser user, List<YsUserAddr> addrs) {
        if (user == null) {
            return null;
        }
        return new UserAggregate(
                org.ys.transaction.domain.entity.YsUser.rehydrate(
                        user.getId(),
                        user.getUsername(),
                        user.getPassword(),
                        user.getAge(),
                        user.getSex(),
                        user.getBalance(),
                        user.getEmail(),
                        user.getTel(),
                        user.getStatus(),
                        user.getCreateTime()
                ),
                addrs
        );
    }


}
