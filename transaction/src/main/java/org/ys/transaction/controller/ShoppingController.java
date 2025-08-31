package org.ys.transaction.controller;


import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.ys.commens.pojo.CommentResult;
import org.ys.commens.vo.CartItem;
import org.ys.shoppingcar.CartService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
public class ShoppingController {
    @Resource
    private CartService service;

    @RequestMapping("addCart")
    @ResponseBody
    public CommentResult addCart(@RequestBody CartItem ysGoods){
        return service.addCart(ysGoods);
    }

    @RequestMapping("showCart")
    @ResponseBody
    public CommentResult showCart(@RequestBody Map<String, Object> map){
        //分页查询
        return  service.showCart(Long.valueOf((String) map.get("userId")));
    }


    @RequestMapping("deleteById")
    @ResponseBody
    public CommentResult deleteById(@RequestBody Map<String, Object> map){
        return  service.deleteById(Long.valueOf((String) map.get("itemId")),Long.valueOf((String) map.get("userId")));
    }


    /**
     * 普通商品结算接口
     * @param maps
     * @return
     */
    @RequestMapping("goSettlement")
    @ResponseBody
    public CommentResult goSettlement(@RequestBody List<Map<String, Object>> maps){
        return  service.goSettlement(maps);
    }

    /**
     * 秒杀商品结算接口
     * @param map
     * @return
     */
    @RequestMapping("goSeckillSettlement")
    @ResponseBody
    public CommentResult goSeckillSettlement(@RequestBody Map<String, Object> map){
        return  service.goSeckillSettlement((Long) map.get("itemId"),Long.valueOf((String) map.get("userId")));
    }

    /**
     * 秒杀商品
     * @param map
     * @return
     */
    @RequestMapping("seckill")
    @ResponseBody
    public CommentResult seckill(@RequestBody Map<String, Object> map){
        return  service.seckill(Long.valueOf((Long) map.get("itemId")),Long.valueOf((Long) map.get("userId")));
    }

    /**
     * 预热秒杀商品
     * @param map
     * @return
     */
    @RequestMapping("initSeckillItem")
    @ResponseBody
    public CommentResult initSeckillItem(@RequestBody Map<String, Object> map){
        return  service.initSeckillItem( Long.parseLong(map.get("itemId").toString()),new BigDecimal(100),
                Integer.valueOf(map.get("num").toString()),Long.valueOf(map.get("expireTime").toString()));
    }


}
