
package org.ys.commens.config;


import jakarta.jms.ConnectionFactory;
import jakarta.jms.JMSException;
import jakarta.jms.Session;
import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.util.StringUtils;

/**
 * ActiveMQ Artemis配置类，用于配置消息队列连接工厂、JMS模板和监听容器工厂
 */
@Configuration
@EnableJms
public class ActiveMQConfig {

    @Value("${spring.artemis.broker-url:tcp://localhost:61616}")
    private String brokerUrl;

    @Value("${spring.artemis.user:admin}")
    private String username;

    @Value("${spring.artemis.password:admin}")
    private String password;

    /**
     * 创建ActiveMQ Artemis连接工厂
     * @return ConnectionFactory 连接工厂实例
     */
    @Bean
    public ConnectionFactory connectionFactory() throws JMSException {
        ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory();
        connectionFactory.setBrokerURL(brokerUrl);

        // 只有当用户名和密码不为空时才设置认证信息
        if (StringUtils.hasText(username) && StringUtils.hasText(password)) {
            connectionFactory.setUser(username);
            connectionFactory.setPassword(password);
        }

        // 设置兼容性参数以解决版本不匹配问题
        connectionFactory.setUseGlobalPools(false);
        connectionFactory.setCacheDestinations(true);
        connectionFactory.setConnectionTTL(-1);
        connectionFactory.setClientID("ecommerce-client");

        return connectionFactory;
    }

    /**
     * 创建JMS模板，用于发送消息
     * @return JmsTemplate JMS模板实例
     */
    @Bean
    public JmsTemplate jmsTemplate() throws JMSException {
        JmsTemplate template = new JmsTemplate();
        template.setConnectionFactory(connectionFactory());
        template.setDefaultDestinationName("seckill.order.queue");
        // 设置消息持久化
        template.setDeliveryPersistent(true);
        // 设置确认模式
        template.setSessionAcknowledgeMode(Session.CLIENT_ACKNOWLEDGE);
        return template;
    }

    /**
     * 创建JMS监听容器工厂，用于接收消息
     * @return DefaultJmsListenerContainerFactory 监听容器工厂实例
     */
    @Bean
    public DefaultJmsListenerContainerFactory jmsListenerContainerFactory() throws JMSException {
        DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory());
        factory.setConcurrency("1-1");

        // 设置确认模式为客户端确认
        factory.setSessionAcknowledgeMode(Session.CLIENT_ACKNOWLEDGE);
         // 设置目的地解析器以提高兼容性
        factory.setPubSubDomain(false);
        return factory;
    }
}