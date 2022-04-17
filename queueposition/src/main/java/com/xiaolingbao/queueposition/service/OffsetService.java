package com.xiaolingbao.queueposition.service;

import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.store.OffsetStore;
import org.apache.rocketmq.client.consumer.store.ReadOffsetType;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.common.message.MessageQueue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;

@Service
public class OffsetService {
    @Autowired
    @Qualifier("consumer")
    private DefaultMQPushConsumer defaultMQPushConsumer;

    @Autowired
    @Qualifier("asyncProducer")
    private DefaultMQProducer defaultMQProducer;

    private OffsetStore offsetStore;

    private List<MessageQueue> messageQueueList;

    public long getConsumerPosition(MessageQueue queue) {
        try {
//            messageQueueList = defaultMQProducer.fetchPublishMessageQueues("mqtestTopic");
            offsetStore = defaultMQPushConsumer.getOffsetStore();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return offsetStore.readOffset(queue, ReadOffsetType.MEMORY_FIRST_THEN_STORE);
    }


}
