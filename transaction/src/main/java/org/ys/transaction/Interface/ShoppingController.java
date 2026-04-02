package org.ys.transaction.Interface;


import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.ys.transaction.Interface.VO.CommentResult;
import org.ys.transaction.application.ShoppingApplicationService;
import org.ys.transaction.domain.vo.CartItem;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Map;

@RequestMapping("shoppingCar")
@RestController
public class ShoppingController {
    @Resource
    private ShoppingApplicationService service;

    @RequestMapping("addCart")
    @ResponseBody
    public CommentResult addCart(@RequestBody CartItem ysGoods){
        try {
            service.addCart(ysGoods);
            return CommentResult.success();
        } catch (Exception e) {
            return CommentResult.error(e.getMessage());
        }
    }

    @RequestMapping("showCart")
    @ResponseBody
    public CommentResult showCart(@RequestBody Map<String, Object> map){
        try {
            return CommentResult.success(service.showCart(Long.valueOf(map.get("userId").toString())));
        } catch (Exception e) {
            return CommentResult.error(e.getMessage());
        }
    }

    @RequestMapping("deleteById")
    @ResponseBody
    public CommentResult deleteById(@RequestBody Map<String, Object> map){
        try {
            service.deleteById(Long.valueOf(map.get("itemId").toString()),Long.valueOf(map.get("userId").toString()));
            return CommentResult.success();
        } catch (Exception e) {
            return CommentResult.error(e.getMessage());
        }
    }

    /**
     * 更新购物车商品数量
     */
    @RequestMapping("updateCartNum")
    @ResponseBody
    public CommentResult updateCartNum(@RequestBody Map<String, Object> map){
        Long itemId = Long.valueOf(map.get("itemId").toString());
        Long userId = Long.valueOf(map.get("userId").toString());
        Integer num = Integer.valueOf(map.get("num").toString());
        try {
            service.updateCartNum(itemId, userId, num);
            return CommentResult.success("数量更新成功");
        } catch (Exception e) {
            return CommentResult.error(e.getMessage());
        }
    }


    /**
     * 普通商品结算订单接口
     * @param items  商品id集合  用户id
     * @return
     */
    @RequestMapping("goSettlement")
    @ResponseBody
    public CommentResult goSettlement(@RequestBody Map<String, Object> items){
        try {
            return CommentResult.success(service.goSettlement(items));
        } catch (Exception e) {
            return CommentResult.error(e.getMessage());
        }
    }

    @RequestMapping("showOrder")
    @ResponseBody
    public CommentResult showOrder(@RequestBody Map<String, Object> map){
        try {
            return CommentResult.success(service.showOrder(Long.valueOf(map.get("userId").toString())));
        } catch (Exception e) {
            return CommentResult.error(e.getMessage());
        }
    }
    /**
     * 普通商品结算订单接口
     * @param item  订单id  用户id
     * @return
     */
    @RequestMapping("goPay")
    @ResponseBody
    public CommentResult goPay(@RequestBody Map<String, Object> item){
        String userId = item.get("userId").toString();
        String[] orderIds = item.get("orderIds").toString().split(",");
        try {
            service.goPay(userId, orderIds);
            return CommentResult.success();
        } catch (Exception e) {
            return CommentResult.error(e.getMessage());
        }
    }

    /**
     * 秒杀商品结算接口
     * @param map
     * @return
     */
    @RequestMapping("goSeckillSettlement")
    @ResponseBody
    public CommentResult goSeckillSettlement(@RequestBody Map<String, Object> map) throws JsonProcessingException {
        try {
            service.goSeckillSettlement(map.get("itemId").toString(),map.get("userId").toString());
            return CommentResult.success();
        } catch (Exception e) {
            return CommentResult.error(e.getMessage());
        }
    }

    /**
     * 秒杀商品
     * @param map
     * @return
     */
    @RequestMapping("seckill")
    @ResponseBody
    public CommentResult seckill(@RequestBody Map<String, Object> map){
        try {
            service.seckill(map.get("itemId").toString(),map.get("userId").toString());
            return CommentResult.success("秒杀成功");
        } catch (Exception e) {
            return CommentResult.error(e.getMessage());
        }
    }

    /**
     * 预热秒杀商品
     * @param map
     * @return
     */
    @RequestMapping("initSeckillItem")
    @ResponseBody
    public CommentResult initSeckillItem(@RequestBody Map<String, Object> map){
        try {
            service.initSeckillItem(map.get("itemId").toString(),new BigDecimal(100),
                    Integer.valueOf(map.get("num").toString()),map.get("expireTime").toString());
            return CommentResult.success();
        } catch (Exception e) {
            return CommentResult.error(e.getMessage());
        }
    }


}
