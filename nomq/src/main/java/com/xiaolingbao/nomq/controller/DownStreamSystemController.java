package com.xiaolingbao.nomq.controller;

import com.alibaba.fastjson.JSONObject;
import com.xiaolingbao.nomq.service.DownStreamSystemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;

@RestController
public class DownStreamSystemController {

    @Autowired
    private DownStreamSystemService downStreamSystemService;



    @RequestMapping("/cal")
    public JSONObject calculate(@RequestParam String ID) {
        Long id = Long.valueOf(ID);
        LocalTime startTime = LocalTime.now();
        System.out.println("接收到id为: " + id + "的请求, 当前时间: " + startTime);
        Long result = null;
        try {
            result = downStreamSystemService.cal(id).get();
            LocalTime endTime = LocalTime.now();
            System.out.println("执行完id为: " + id + "的请求, 当前时间: " + endTime + ", 耗时: " + Duration.between(startTime, endTime).getSeconds()
                    + ", 结果为: " + result);
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("res", result);
        return jsonObject;
    }

}
