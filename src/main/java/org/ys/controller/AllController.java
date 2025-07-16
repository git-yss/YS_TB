package org.ys.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.ys.service.AllService;

import java.util.Map;

@RestController
public class AllController {
    @Autowired
    private AllService service;

    @RequestMapping("login")
    @ResponseBody
    public void login(@RequestBody Map<String, Object> map){
        service.login(map);
    }
}
