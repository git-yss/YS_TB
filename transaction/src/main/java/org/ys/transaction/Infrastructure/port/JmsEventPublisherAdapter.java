package org.ys.transaction.Infrastructure.port;

import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;
import org.ys.transaction.domain.port.EventPublisherPort;

import javax.annotation.Resource;

@Component
public class JmsEventPublisherAdapter implements EventPublisherPort {

    @Resource
    private JmsTemplate jmsTemplate;

    @Override
    public void publish(String topicOrQueue, Object payload) {
        jmsTemplate.convertAndSend(topicOrQueue, payload);
    }
}

