package org.ys.transaction.domain.inteface;


import com.fasterxml.jackson.core.JsonProcessingException;
import org.ys.transaction.domain.vo.CartItem;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public interface CartService {
    /**
     * 添加购物车
     * @param itemId
     * @param num
     * @return
     */
    void addCart(CartItem ysGoods);

    /**
     * 显示购物车
      * @return
     */
    List<CartItem> showCart(Long userId);



    /**
     * 删除购物车数据
     * @param itemId
     * @return
     */
    void deleteById(Long itemId, Long userId);

    /**
     * 更新购物车中商品数量（Redis 为准）
     *
     * @param itemId 商品ID
     * @param userId 用户ID
     * @param num 新数量
     */
    void updateCartNum(Long itemId, Long userId, Integer num);

    /**
     * 普通商品结算订单
     * @param items  商品id集合  用户id
     * @return
     */
    Long goSettlement(Map<String, Object> items);

    /**
     * 普通商品支付订单
     * @param userId  orderId 用户id  订单id
     * @return
     */
    void goPay(String userId, String[] orderIds);

    /**
     * 秒杀单品
     * @param userId
     * @param itemId
     * @return
     */
    void seckill(String itemId, String userId);

    /**
     * 生成订单
     * @param cartItem
     * @return
     */
    void addOrder(CartItem  cartItem,String way);

    void addOrderCpnt(CartItem  cartItem);

    void addOrderNormal(ArrayList<CartItem> cartItem,String way);

    void addOrderNormalCpnt(String orderId,CartItem item );

    /**
     * 初始化秒杀商品并设置过期时间
     * @param itemId 商品ID
     * @param expireTime 过期时间（秒）
     */
    void initSeckillItem(String itemId, BigDecimal price, int num, String expireTime);

    /**
     * 秒杀结算
     * @param itemId
     * @param userId
     * @return
     */
    void goSeckillSettlement(String itemId, String userId) throws JsonProcessingException;

    Object showOrder(Long userId);
}
