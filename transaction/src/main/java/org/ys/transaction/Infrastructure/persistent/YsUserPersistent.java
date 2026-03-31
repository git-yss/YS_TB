package org.ys.transaction.Infrastructure.persistent;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.ys.transaction.domain.aggregate.LoginAggregate;
import org.ys.transaction.domain.aggregate.UserAggregate;
import org.ys.transaction.domain.respository.YsUserRespository;
import org.ys.transaction.Infrastructure.dao.YsUserDao;
import org.ys.transaction.Infrastructure.pojo.YsUser;

@Repository
@RequiredArgsConstructor
public class YsUserPersistent implements YsUserRespository {
    private final YsUserDao ysUserDao;

    @Override
    public LoginAggregate queryUser(LoginAggregate aggregate) {
        YsUser po = ysUserDao.queryUser(aggregate.getUser().getUsername(), aggregate.getUser().getPassword());
        if (po == null) return null;
        return new LoginAggregate(toEntity(po));
    }

    @Override
    public UserAggregate selectAggregateById(UserAggregate aggregate) {
        YsUser po = ysUserDao.selectById(aggregate.getUser().getId());
        if (po == null) return null;
        return new UserAggregate(toEntity(po), aggregate.getAddresses());
    }

    @Override
    public UserAggregate selectByName(UserAggregate aggregate) {
        YsUser po = ysUserDao.selectByName(aggregate.getUser().getUsername());
        if (po == null) return null;
        return new UserAggregate(toEntity(po), aggregate.getAddresses());
    }

    @Override
    public UserAggregate selectByEmail(UserAggregate aggregate) {
        YsUser po = ysUserDao.selectByEmail(aggregate.getUser().getEmail());
        if (po == null) return null;
        return new UserAggregate(toEntity(po), aggregate.getAddresses());
    }

    @Override
    public int updateBalanceById(UserAggregate aggregate) {
        return ysUserDao.updateBalanceById(toPo(aggregate.getUser()));
    }

    @Override
    public int insert(UserAggregate aggregate) {
        return ysUserDao.insert(toPo(aggregate.getUser()));
    }

    private org.ys.transaction.domain.entity.YsUser toEntity(YsUser po) {
        return org.ys.transaction.domain.entity.YsUser.rehydrate(
                po.getId(), po.getUsername(), po.getPassword(), po.getAge(), po.getSex(),
                po.getBalance(), po.getEmail(), po.getTel(), po.getStatus(), po.getCreateTime()
        );
    }

    private YsUser toPo(org.ys.transaction.domain.entity.YsUser e) {
        YsUser po = new YsUser();
        po.setId(e.getId());
        po.setUsername(e.getUsername());
        po.setPassword(e.getPassword());
        po.setAge(e.getAge());
        po.setSex(e.getSex());
        po.setBalance(e.getBalance());
        po.setEmail(e.getEmail());
        po.setTel(e.getTel());
        po.setStatus(e.getStatus());
        po.setCreateTime(e.getCreateTime());
        return po;
    }
}
