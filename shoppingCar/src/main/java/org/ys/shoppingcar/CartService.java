package org.ys.shoppingcar;


import com.fasterxml.jackson.core.JsonProcessingException;
import org.ys.commens.pojo.CommentResult;
import org.ys.commens.vo.CartItem;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface CartService {
    /**
     * 添加购物车
     * @param itemId
     * @param num
     * @return
     */
    CommentResult addCart(CartItem ysGoods);

    /**
     * 显示购物车
      * @return
     */
    CommentResult showCart(Long userId);



    /**
     * 删除购物车数据
     * @param itemId
     * @return
     */
    CommentResult deleteById(Long itemId, Long userId);

    /**
     * 普通支付
     * @param maps
     * @return
     */
    CommentResult goSettlement(List<Map<String, Object>> maps);

    /**
     * 秒杀单品
     * @param userId
     * @param itemId
     * @return
     */
    CommentResult seckill(String itemId,String userId);

    /**
     * 生成订单
     * @param cartItem
     * @return
     */
    void addOrder(CartItem  cartItem,int code);

    void addOrderCpnt(CartItem  cartItem);

    /**
     * 初始化秒杀商品并设置过期时间
     * @param itemId 商品ID
     * @param expireTime 过期时间（秒）
     */
    CommentResult initSeckillItem(String itemId, BigDecimal price,int num, String expireTime);

    /**
     * 秒杀结算
     * @param itemId
     * @param userId
     * @return
     */
    CommentResult goSeckillSettlement(String itemId, String userId) throws JsonProcessingException;
}
