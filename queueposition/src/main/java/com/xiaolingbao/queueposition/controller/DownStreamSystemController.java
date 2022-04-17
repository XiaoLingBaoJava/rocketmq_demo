package com.xiaolingbao.queueposition.controller;

import cn.hutool.core.map.MapUtil;
import com.alibaba.fastjson.JSONObject;
import com.xiaolingbao.queueposition.common.Result;
import com.xiaolingbao.queueposition.service.AsyncProducerService;
import com.xiaolingbao.queueposition.service.DownStreamSystemService;
import com.xiaolingbao.queueposition.utils.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalTime;
import java.util.concurrent.FutureTask;

@RestController
public class DownStreamSystemController {

    @Autowired
    private AsyncProducerService asyncProducerService;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private DownStreamSystemService downStreamSystemService;



    @RequestMapping("/cal")
    public Result calculate(@RequestParam String ID) {
        LocalTime startTime = LocalTime.now();
        System.out.println("接收到id为: " + ID + "的请求, 当前时间: " + startTime);
        String redisKey = ID + System.currentTimeMillis() / 1000L;
        try {
            asyncProducerService.sendMessage(ID, startTime, redisKey, "mqtestTopic", "mqtestTag", ID.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Result.succ(MapUtil.builder().put("redisKey", redisKey).build());
    }

    @GetMapping("/cancel")
    public Result cancel(@RequestParam String redisKey) {
        redisUtil.hset(redisKey, "isCancel", true);
        FutureTask<Long> task = downStreamSystemService.getTaskUsingRedisKey(redisKey);
        if (task != null) {
            task.cancel(true);
        }
        return Result.succ(MapUtil.builder().put("redisKey", redisKey).build());
    }

}
