package com.xiaolingbao.queueposition.service;

import cn.hutool.core.map.MapUtil;
import com.xiaolingbao.queueposition.utils.RedisUtil;
import com.xiaolingbao.queueposition.websocket.WebSocketClient;
import com.xiaolingbao.queueposition.websocket.WebSocketService;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageQueue;
import org.apache.rocketmq.remoting.exception.RemotingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AsyncProducerService {

    @Autowired
    @Qualifier("asyncProducer")
    private DefaultMQProducer defaultMQProducer;

    @Autowired
    private OffsetService offsetService;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private WebSocketService webSocketService;

    private final long messageConsumeAvgTime = 60;

    private ConcurrentHashMap<String, WebSocketClient> webSocketMap;

    @PostConstruct
    public void init() {
        try {
            defaultMQProducer.start();
        } catch (MQClientException e) {
            e.printStackTrace();
        }
        webSocketMap = webSocketService.getWebSocketMap();
    }

    public void sendMessage(String id, LocalTime startTime, String redisKey, String topic, String tag, byte[] body) {
        Message message = new Message(topic, tag, body);
        message.putUserProperty("id", id);
        message.putUserProperty("startTime", startTime.toString());
        message.setKeys(redisKey);
        try {
            defaultMQProducer.send(message, new SendCallback() {
                @Override
                public void onSuccess(SendResult sendResult) {
                    HashMap<String, Object> hashMap = new HashMap<>(2);
                    hashMap.put("isCancel", false);
                    hashMap.put("isFirst", true);
                    redisUtil.hmset(message.getKeys(), hashMap);

                    System.out.println(sendResult);
                    long offset = sendResult.getQueueOffset();
                    MessageQueue messageQueue = sendResult.getMessageQueue();
                    System.out.println("id为: " + id + "queue id为: " + messageQueue.getQueueId() + ", 消息在队列中的offset: " + offset
                    + ", 当前消费者消费到的offset: " + offsetService.getConsumerPosition(messageQueue));
                    long positionInQueue = offset - offsetService.getConsumerPosition(messageQueue) + 1;
                    while (!webSocketMap.containsKey(id)) {
                        synchronized (webSocketMap) {
                            try {
                                webSocketMap.wait();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    WebSocketService.sendMessage(id, MapUtil.builder().put("minWaitTime", positionInQueue * 60).
                            put("maxWaitTime", positionInQueue * 60 * defaultMQProducer.getDefaultTopicQueueNums())
                            .put("positionInQueue", positionInQueue).build());
                    System.out.println("id : " + id + ", send websocket wait info success");
                }

                @Override
                public void onException(Throwable e) {
                    e.printStackTrace();
                }
            });
        } catch (MQClientException | RemotingException | InterruptedException e) {
            e.printStackTrace();
        }
    }


}
