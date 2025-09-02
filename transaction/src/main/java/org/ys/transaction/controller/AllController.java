package org.ys.transaction.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.ys.commens.entity.YsGoods;
import org.ys.commens.pojo.CommentResult;
import org.ys.transaction.service.IndexService;


import java.util.Map;

@RestController
public class AllController {
    @Autowired
    private IndexService service;

    @RequestMapping("login")
    @ResponseBody
    public CommentResult login(@RequestBody Map<String, Object> map){
        service.login(map);
        return new CommentResult().ok();
    }

    @RequestMapping("queryAllGoodsPage")
    @ResponseBody
    public CommentResult queryAllGoods(@RequestBody Map<String, Object> map){
        //分页查询
        Page<YsGoods> list =  service.queryAllGoods(map);
        return new CommentResult().ok(list);
    }
}
