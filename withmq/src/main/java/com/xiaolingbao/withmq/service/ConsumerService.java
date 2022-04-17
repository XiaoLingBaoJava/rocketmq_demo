package com.xiaolingbao.withmq.service;

import cn.hutool.core.map.MapUtil;
import com.xiaolingbao.withmq.websocket.WebSocketService;
import org.apache.commons.collections.MapUtils;
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
    DefaultMQPushConsumer defaultMQPushConsumer;

    @Autowired
    DownStreamSystemService downStreamSystemService;


    @PostConstruct
    public void init() {
        try {
            defaultMQPushConsumer.registerMessageListener(new MessageListenerConcurrently() {
                @Override
                public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs, ConsumeConcurrentlyContext context) {
                    // 逐条消费消息
                    for (MessageExt msg : msgs) {

                        String id = msg.getUserProperty("id");
                        System.out.println("开始执行id为: " + id + "的请求");
                        Long result = null;
                        try {
                            result = downStreamSystemService.cal(Long.valueOf(id)).get();
                        } catch (InterruptedException | ExecutionException e) {
                            e.printStackTrace();
                        }
                        System.out.println(msg);
                        LocalTime startTime = LocalTime.parse(msg.getUserProperty("startTime"));
                        LocalTime endTime = LocalTime.now();
                        System.out.println("执行完id为: " + id + "的请求, 当前时间: " + endTime + ", 耗时: " + Duration.between(startTime, endTime).getSeconds()
                                + ", 结果为: " + result);
                        WebSocketService.sendMessage(id, MapUtil.builder().put(id, result).build());
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
