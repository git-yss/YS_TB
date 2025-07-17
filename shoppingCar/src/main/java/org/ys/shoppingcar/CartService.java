package org.ys.shoppingcar;

import org.ys.commens.pojo.CommentResult;

import java.util.List;

public interface CartService {
    /**
     * 添加购物车
     * @param itemId
     * @param num
     * @return
     */
    CommentResult addCart(Long itemId, Integer num);

    /**
     * 显示购物车
      * @return
     */
    CommentResult showCart();

    /**
     * 修改购物车中购买数量
     * @param itemId
     * @param num
     * @return
     */
    CommentResult updateNum(Long itemId,Integer num);

    /**
     * 删除购物车数据
     * @param itemId
     * @return
     */
    CommentResult deleteById(Long itemId);

    /**
     * 去结算
     * @param ids
     * @return
     */
    CommentResult goSettlement(List<Long> ids);
}
