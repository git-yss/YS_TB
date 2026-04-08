package org.ys.transaction.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;
import org.ys.transaction.domain.service.CartDomainService;
import org.ys.transaction.domain.vo.CartItem;

import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Service
public class ShoppingApplicationService {

    @Resource
    private CartDomainService cartDomainService;

    @Resource
    private OrderApplicationService orderApplicationService;

    public void addCart(CartItem item) {
        cartDomainService.addCart(item);
    }

    public List<CartItem> showCart(Long userId) {
        return cartDomainService.showCart(userId);
    }

    public void deleteById(Long itemId, Long userId) {
        cartDomainService.deleteById(itemId, userId);
    }

    public void updateCartNum(Long itemId, Long userId, Integer num) {
        cartDomainService.updateCartNum(itemId, userId, num);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public Long goSettlement(Map<String, Object> items) {
        return cartDomainService.goSettlement(items);
    }

    public Object showOrder(Long userId) {
        return orderApplicationService.list(userId);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void goPay(String userId, String[] orderIds) {
        cartDomainService.goPay(userId, orderIds);
    }

    public void goSeckillSettlement(String itemId, String userId) throws JsonProcessingException {
        cartDomainService.goSeckillSettlement(itemId, userId);
    }

    public void seckill(String itemId, String userId) {
        cartDomainService.seckill(itemId, userId);
    }

    public void initSeckillItem(String itemId, BigDecimal price, int num, String expireTime) {
        cartDomainService.initSeckillItem(itemId, price, num, expireTime);
    }
}

