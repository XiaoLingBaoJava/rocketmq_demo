package com.xiaolingbao.queueposition.config;

import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.common.consumer.ConsumeFromWhere;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RocketmqConfig {

    @Bean("asyncProducer")
    public DefaultMQProducer asyncProducer() {
        DefaultMQProducer producer = new DefaultMQProducer("producerGroup");
        producer.setNamesrvAddr("192.168.20.100:9876");

        // 指定异步发送失败后不进行重试发送
        producer.setRetryTimesWhenSendAsyncFailed(0);

        // 指定新创建的Topic的Queue数量为2，默认为4
        producer.setDefaultTopicQueueNums(2);

        return producer;
    }

    @Bean("consumer")
    public DefaultMQPushConsumer consumer() {
        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer("consumerGroup");
        // 指定NameServer
        consumer.setNamesrvAddr("192.168.20.100:9876");
        // 指定从第一条消息开始消费
        consumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_FIRST_OFFSET);
        // 指定消费topic与tag
        try {
            consumer.subscribe("mqtestTopic", "*");
        } catch (MQClientException e) {
            e.printStackTrace();
        }
        return consumer;
    }



}
