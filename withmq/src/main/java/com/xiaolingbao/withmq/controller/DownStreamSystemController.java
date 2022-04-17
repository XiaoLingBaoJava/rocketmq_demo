package com.xiaolingbao.withmq.controller;

import com.alibaba.fastjson.JSONObject;
import com.xiaolingbao.withmq.service.AsyncProducerService;
import com.xiaolingbao.withmq.service.DownStreamSystemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.time.LocalTime;
import java.util.concurrent.ExecutionException;

@RestController
public class DownStreamSystemController {

    @Autowired
    private AsyncProducerService asyncProducerService;



    @RequestMapping("/cal")
    public JSONObject calculate(@RequestParam String ID) {
        LocalTime startTime = LocalTime.now();
        System.out.println("接收到id为: " + ID + "的请求, 当前时间: " + startTime);
        try {
            asyncProducerService.sendMessage(ID, startTime, "mqtestTopic", "mqtestTag", ID.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("state", "succ");
        return jsonObject;
    }

}
