package org.ys.transaction.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.ys.commens.pojo.CommentResult;
import org.ys.transaction.service.AllService;


import java.util.Map;

@RestController
public class AllController {
    @Autowired
    private AllService service;

    @RequestMapping("login")
    @ResponseBody
    public CommentResult login(@RequestBody Map<String, Object> map){
        service.login(map);
        return new CommentResult().ok();
    }
}
