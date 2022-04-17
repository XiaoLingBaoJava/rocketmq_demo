package com.xiaolingbao.queueposition.service;

import cn.hutool.core.map.MapUtil;
import com.xiaolingbao.queueposition.utils.RedisUtil;
import com.xiaolingbao.queueposition.websocket.WebSocketService;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.message.MessageExt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.time.LocalTime;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Service
public class ConsumerService {

    @Autowired
    @Qualifier("consumer")
    private DefaultMQPushConsumer defaultMQPushConsumer;

    @Autowired
    private DownStreamSystemService downStreamSystemService;

    @Autowired
    private RedisUtil redisUtil;


    @PostConstruct
    public void init() {
        try {
            defaultMQPushConsumer.registerMessageListener(new MessageListenerConcurrently() {
                @Override
                public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs, ConsumeConcurrentlyContext context) {
                    // 逐条消费消息
                    for (MessageExt msg : msgs) {
                        System.out.println("redisKey isFirst: " + (boolean)redisUtil.hmget(msg.getKeys()).get("isFirst"));
                        System.out.println("redisKey isCancel: " + (boolean)redisUtil.hmget(msg.getKeys()).get("isCancel"));

                        if (!(boolean)redisUtil.hmget(msg.getKeys()).get("isFirst") || (boolean)redisUtil.hmget(msg.getKeys()).get("isCancel")) {
                            continue;
                        }

                        String id = msg.getUserProperty("id");
                        System.out.println("开始执行id为: " + id + "的请求");
                        Long result = null;
                        try {
                            result = downStreamSystemService.cal(Long.valueOf(id), msg.getKeys()).get();
                        } catch (InterruptedException | ExecutionException e) {
                            e.printStackTrace();
                        }
                        System.out.println(msg);
                        LocalTime startTime = LocalTime.parse(msg.getUserProperty("startTime"));
                        LocalTime endTime = LocalTime.now();
                        System.out.println("执行完id为: " + id + "的请求, 当前时间: " + endTime + ", 耗时: " + Duration.between(startTime, endTime).getSeconds()
                                + ", 结果为: " + result);
                        WebSocketService.sendMessage(id, MapUtil.builder().put(id, result).build());

                        redisUtil.hset(msg.getKeys(), "isFirst", false);
                    }
                    // 返回消费状态:消费成功
                    return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
                }
            });

            defaultMQPushConsumer.start();
        } catch (MQClientException e) {
            e.printStackTrace();
        }
    }

}
