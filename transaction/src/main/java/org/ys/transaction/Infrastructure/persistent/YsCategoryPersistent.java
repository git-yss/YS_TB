package org.ys.transaction.Infrastructure.persistent;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.ys.transaction.domain.aggregate.CateGoryAggregate;
import org.ys.transaction.domain.respository.YsCategoryRespository;
import org.ys.transaction.Infrastructure.dao.YsCategoryDao;
import org.ys.transaction.Infrastructure.pojo.YsCategory;

import java.util.List;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class YsCategoryPersistent implements YsCategoryRespository {
    private final YsCategoryDao ysCategoryDao;

    @Override
    public List<CateGoryAggregate> selectByParentId(CateGoryAggregate aggregate) {
        Long parentId = aggregate.getYsCategory().getParentId();
        return ysCategoryDao.selectByParentId(parentId).stream()
                .map(this::toAggregate)
                .collect(Collectors.toList());
    }

    @Override
    public List<CateGoryAggregate> selectEnabled(CateGoryAggregate aggregate) {
        return ysCategoryDao.selectEnabled().stream().map(this::toAggregate).collect(Collectors.toList());
    }

    private CateGoryAggregate toAggregate(YsCategory po) {
        return new CateGoryAggregate(org.ys.transaction.domain.entity.YsCategory.rehydrate(
                po.getId(), po.getParentId(), po.getCategoryName(), po.getCategoryCode(), po.getSortOrder(),
                po.getIcon(), po.getDescription(), po.getCreatedAt(), po.getUpdatedAt(), po.getStatus()
        ));
    }
}
