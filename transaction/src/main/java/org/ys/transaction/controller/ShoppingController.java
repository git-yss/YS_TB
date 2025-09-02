package org.ys.transaction.controller;


import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.ys.commens.pojo.CommentResult;
import org.ys.commens.vo.CartItem;
import org.ys.transaction.service.CartService;

import javax.annotation.Resource;
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
        return  service.showCart(Long.valueOf(map.get("userId").toString()));
    }

    @RequestMapping("deleteById")
    @ResponseBody
    public CommentResult deleteById(@RequestBody Map<String, Object> map){
        return  service.deleteById(Long.valueOf(map.get("itemId").toString()),Long.valueOf(map.get("userId").toString()));
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
    public CommentResult goSeckillSettlement(@RequestBody Map<String, Object> map) throws JsonProcessingException {
        return  service.goSeckillSettlement(map.get("itemId").toString(),map.get("userId").toString());
    }

    /**
     * 秒杀商品
     * @param map
     * @return
     */
    @RequestMapping("seckill")
    @ResponseBody
    public CommentResult seckill(@RequestBody Map<String, Object> map){
        return  service.seckill(map.get("itemId").toString(),map.get("userId").toString());
    }

    /**
     * 预热秒杀商品
     * @param map
     * @return
     */
    @RequestMapping("initSeckillItem")
    @ResponseBody
    public CommentResult initSeckillItem(@RequestBody Map<String, Object> map){
        return  service.initSeckillItem(map.get("itemId").toString(),new BigDecimal(100),
                Integer.valueOf(map.get("num").toString()),map.get("expireTime").toString());
    }


}
