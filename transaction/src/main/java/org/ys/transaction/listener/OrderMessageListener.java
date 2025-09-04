package org.ys.transaction.listener;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;
import org.ys.commens.dao.YsOrderDao;
import org.ys.commens.utils.JsonUtils;
import org.ys.commens.vo.CartItem;
import org.ys.transaction.service.CartService;
import javax.annotation.Resource;
import javax.jms.JMSException;
import javax.jms.Message;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 秒杀场景消息监听进行新增订单和结算时发送邮件
 */
@Component
public class OrderMessageListener {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(OrderMessageListener.class);
    @Resource
    private CartService cartService;

    @Resource
    private YsOrderDao ysOrderDao;

    @Resource
    private JmsTemplate jmsTemplate;


    @Resource
    private JavaMailSender mailSender;



    // 最大重试次数
    private static final int MAX_RETRY_COUNT = 3;

    @JmsListener(destination = "seckill.order.queue", containerFactory = "jmsListenerContainerFactory")
    public void receiveOrderMessage(String cartItemJson, Message message) {
        System.out.println("接收到秒杀订单消息: " + cartItemJson);
        CartItem cartItem = null;
        cartItem = JsonUtils.jsonToPojo(cartItemJson, CartItem.class);
        // 新增购物订单
        try {
            // 处理订单消息
            cartItem.setNum(1);//秒杀场景都是扣1个
            cartService.addOrder(cartItem,"");
            // 手动确认消息
            message.acknowledge();
            log.info("订单处理成功: 用户ID={}, 商品ID={}", cartItem.getUserId(), cartItem.getItemId());
        } catch (DuplicateKeyException e) {
            //订单防悬挂，这里不处理直接返回

        }catch (Exception e) {
            // 发生异常时，消息会重新入队
            log.error("订单处理失败: {}", cartItem.getItemId(), e);
            try {
                // 将消息重新入队，等待下次处理
                // 这里可以设置重试次数限制
                int retryCount = getRetryCount(message);
                log.info("当前重试次数: {}", retryCount);
                if (retryCount < MAX_RETRY_COUNT) {
                    // 重新发送消息
                    requeueMessage(cartItem, retryCount + 1,"seckill.order.queue");
                } else {
                    // 超过重试次数，转入死信队列或人工处理
                    moveToDLQ(cartItem);
                    log.error("超过最大重试次数，消息转入死信队列: 用户ID={}, 商品ID={}",
                            cartItem.getUserId(), cartItem.getItemId());
                }
            } catch (JMSException jmsEx) {
                log.error("消息重入队失败: {}", cartItem.getItemId(), jmsEx);
            }
        }
    }

    @JmsListener(destination = "mail.queue", containerFactory = "jmsListenerContainerFactory")
    public void sendEmailMessage(long orderId, Message message) {
        System.out.println("接收到发送邮件消息，订单号： " + orderId);
        // 新增购物订单
        try {
            // 处理订单消息
            List<Map<String, Object>> maps = ysOrderDao.selectDetailById(orderId);
            String userEmail = maps.get(0).get("email").toString();
            String introduce = maps.stream().map(map -> map.get("introduce").toString()).collect(Collectors.joining("、"));
            // 手动确认消息
            message.acknowledge();
            SimpleMailMessage email = new SimpleMailMessage();
            email.setFrom("811570083@qq.com");
            email.setTo(userEmail);
            email.setSubject("交易提示");
            email.setText(String.format("订单号：%s 的用户 %s 购买了 %s 操作，已经购买成功，将会在三个工作日内发送到 %s ", orderId, maps.get(0).get("username").toString(), introduce, maps.get(0).get("addr").toString()));
            mailSender.send(email);
            log.info(String.format("订单号：%s 的用户 %s 购买了 %s 操作，已经购买成功，将会在三个工作日内发送到 %s ", orderId, maps.get(0).get("username").toString(), introduce, maps.get(0).get("addr").toString()));
        } catch (Exception e) {
            // 发生异常时，消息会重新入队
            log.error("邮件发送失败: {}", e);
            try {
                // 将消息重新入队，等待下次处理
                // 这里可以设置重试次数限制
                int retryCount = getRetryCount(message);
                log.info("当前重试次数: {}", retryCount);
                if (retryCount < MAX_RETRY_COUNT) {
                    // 重新发送消息
                    requeueMessage(orderId, retryCount + 1,"mail.queue");
                } else {
                    // 超过重试次数，转入死信队列或人工处理
                    moveToDLQ(orderId);
                    log.error("超过最大重试次数，消息转入死信队列: 订单ID={}",
                            orderId);
                }
            } catch (JMSException jmsEx) {
                log.error("消息重入队失败: {}", orderId, jmsEx);
            }
        }
    }
    /**
     * 获取消息的重试次数
     * @param message JMS消息
     * @return 重试次数
     */
    private int getRetryCount(Message message) {
        try {
            // 尝试从消息属性中获取重试次数
            if (message.propertyExists("retryCount")) {
                return message.getIntProperty("retryCount");
            } else {
                // 如果属性不存在，默认为0
                return 0;
            }
        } catch (JMSException e) {
            // 如果获取失败，默认为0
            return 0;
        }
    }

    /**
     * 重新发送消息到队列
     * @param obj 信息
     * @param retryCount 重试次数
     * @throws JMSException JMS异常
     */
    private void requeueMessage(Object obj, int retryCount,String queueName) throws JMSException {
        // 发送消息到原始队列，并设置重试次数属性
        // 将对象序列化为JSON字符串
        String jsonString = JsonUtils.objectToJson(obj);
        // 添加重试次数属性到JMS消息头
        jmsTemplate.send(queueName, session -> {
            Message message = session.createTextMessage(jsonString);
            message.setIntProperty("retryCount", retryCount);
            // 设置延迟时间，避免立即重试
            message.setLongProperty("_AMQ_SCHED_DELIVERY", System.currentTimeMillis() + (retryCount * 10000)); // 延迟重试
            return message;
        });
    }

    /**
     * 转入死信队列
     * @param obj 订单信息
     */
    private void moveToDLQ(Object obj) {
        try {
            // 发送消息到死信队列
            jmsTemplate.convertAndSend("seckill.order.dlq", obj);

        } catch (Exception e) {
            log.error("死信消息处理失败: 消息内容={}", obj, e);
        }
    }
}
