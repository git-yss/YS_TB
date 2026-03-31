package org.ys.transaction.Infrastructure.persistent;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.ys.transaction.domain.aggregate.UserAddrAggregate;
import org.ys.transaction.domain.respository.YsUserAddrRespository;
import org.ys.transaction.Infrastructure.dao.YsUserAddrDao;
import org.ys.transaction.Infrastructure.pojo.YsUserAddr;

@Repository
@RequiredArgsConstructor
public class YsUserAddrPersistent implements YsUserAddrRespository {
    private final YsUserAddrDao ysUserAddrDao;

    @Override
    public int insert(UserAddrAggregate aggregate) {
        YsUserAddr po = new YsUserAddr();
        po.setId(aggregate.getUserAddr().getId());
        po.setUserId(aggregate.getUserAddr().getUserId());
        po.setAddr(aggregate.getUserAddr().getAddr());
        return ysUserAddrDao.insert(po);
    }
}
