package org.ys.transaction.Infrastructure.persistent;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.ys.transaction.Infrastructure.conver.UserConver;
import org.ys.transaction.Infrastructure.pojo.YsUserAddr;
import org.ys.transaction.domain.aggregate.LoginAggregate;
import org.ys.transaction.domain.aggregate.UserAggregate;
import org.ys.transaction.domain.respository.YsUserRespository;
import org.ys.transaction.Infrastructure.dao.YsUserDao;
import org.ys.transaction.Infrastructure.pojo.YsUser;

import java.util.ArrayList;

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
    public UserAggregate selectAggregateById(String userId) {
        YsUser po = ysUserDao.selectById(userId);
        return UserConver.INSTANCE.poToAggregate(po,null);
    }

    @Override
    public UserAggregate selectByName(String username) {
        YsUser po = ysUserDao.selectByName(username);
        return UserConver.INSTANCE.poToAggregate(po,null);
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
    public int updateById(UserAggregate aggregate) {
        return ysUserDao.updateById(toPo(aggregate.getUser()));
    }

    @Override
    public int insert(UserAggregate aggregate) {
        int result = ysUserDao.insert(toPo(aggregate.getUser()));
        if (result <= 0) {
            throw new IllegalStateException("注册失败");
        }
        return result;
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
