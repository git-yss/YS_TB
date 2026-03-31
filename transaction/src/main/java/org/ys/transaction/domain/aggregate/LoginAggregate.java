package org.ys.transaction.domain.aggregate;

import lombok.Getter;
import org.ys.transaction.domain.entity.YsUser;

@Getter
public class LoginAggregate {
    private final YsUser user;

    public LoginAggregate(YsUser user) {
        if (user == null) {
            throw new IllegalArgumentException("user cannot be null");
        }
        this.user = user;
    }
}
