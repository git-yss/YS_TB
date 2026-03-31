package org.ys.transaction.domain.aggregate;

import lombok.Getter;
import org.ys.transaction.domain.entity.YsUserAddr;

@Getter
public class UserAddrAggregate {
    private final YsUserAddr userAddr;

    public UserAddrAggregate(YsUserAddr userAddr) {
        this.userAddr = userAddr;
    }
}
