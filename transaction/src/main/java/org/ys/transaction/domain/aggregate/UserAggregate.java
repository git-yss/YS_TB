package org.ys.transaction.domain.aggregate;

import lombok.Getter;
import org.ys.transaction.domain.entity.YsUser;
import org.ys.transaction.domain.entity.YsUserAddr;

import java.util.Collections;
import java.util.List;

@Getter
public class UserAggregate {
    private final YsUser user;
    private final List<YsUserAddr> addresses;

    public UserAggregate(YsUser user, List<YsUserAddr> addresses) {
        if (user == null) {
            throw new IllegalArgumentException("user cannot be null");
        }
        this.user = user;
        this.addresses = addresses == null ? Collections.emptyList() : Collections.unmodifiableList(addresses);
    }
}
