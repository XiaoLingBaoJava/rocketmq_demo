package com.xiaolingbao.withmq.service;

import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.remoting.exception.RemotingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.time.LocalTime;

@Service
public class AsyncProducerService {

    @Autowired
    @Qualifier("asyncProducer")
    DefaultMQProducer defaultMQProducer;

    @PostConstruct
    public void init() {
        try {
            defaultMQProducer.start();
        } catch (MQClientException e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(String id, LocalTime startTime, String topic, String tag, byte[] body) {
        Message message = new Message(topic, tag, body);
        message.putUserProperty("id", id);
        message.putUserProperty("startTime", startTime.toString());
        try {
            defaultMQProducer.send(message, new SendCallback() {
                @Override
                public void onSuccess(SendResult sendResult) {
                    System.out.println(sendResult);
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
