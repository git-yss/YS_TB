package org.ys.transaction.domain.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.ys.transaction.domain.vo.CartItem;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 购物车/结算/支付/秒杀领域服务（承载业务规则与流程控制）。
 * 应用层只负责调用本服务，不包含 Redis/锁/JSON/MQ 细节。
 */
public interface CartDomainService {
    void addCart(CartItem item);

    List<CartItem> showCart(Long userId);

    void deleteById(Long itemId, Long userId);

    void updateCartNum(Long itemId, Long userId, Integer num);

    Long goSettlement(Map<String, Object> items);

    void goPay(String userId, String[] orderIds);

    void goSeckillSettlement(String itemId, String userId) throws JsonProcessingException;

    void seckill(String itemId, String userId);

    void initSeckillItem(String itemId, BigDecimal price, int num, String expireTime);
}

