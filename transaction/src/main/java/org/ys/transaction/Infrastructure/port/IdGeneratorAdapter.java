package org.ys.transaction.Infrastructure.port;

import org.springframework.stereotype.Component;
import org.ys.transaction.Infrastructure.utils.IDUtils;
import org.ys.transaction.domain.port.IdGeneratorPort;

@Component
public class IdGeneratorAdapter implements IdGeneratorPort {
    @Override
    public long nextOrderId() {
        return IDUtils.genOrderId();
    }
}

