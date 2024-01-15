package org.mengqi.live.user.provider.config;

import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.consumer.ConsumeFromWhere;
import org.apache.rocketmq.common.message.MessageExt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;
import java.util.List;

@Configuration
public class RocketMQConsumerConfig implements InitializingBean {

    private static final Logger LOGGER= LoggerFactory.getLogger(RocketMQConsumerConfig.class);

    @Resource
    private RocketMQConsumerProperties consumerProperties;
    @Override
    public void afterPropertiesSet() throws Exception {
        initConsumer();
    }

    public void initConsumer(){

        try {
            //初始化我们的RocketMQ消费者
            DefaultMQPushConsumer defaultMQPushConsumer=new DefaultMQPushConsumer();
            defaultMQPushConsumer.setNamesrvAddr(consumerProperties.getNameSrv());
            defaultMQPushConsumer.setConsumerGroup(consumerProperties.getGroupName());
            defaultMQPushConsumer.setConsumeMessageBatchMaxSize(1);
            defaultMQPushConsumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_FIRST_OFFSET);
            defaultMQPushConsumer.subscribe("user-update-cache","*");
            defaultMQPushConsumer.setMessageListener(new MessageListenerConcurrently() {
                @Override
                public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> list, ConsumeConcurrentlyContext consumeConcurrentlyContext) {
                    System.out.println(list.get(0).getBody());
                    return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
                }
            });
            defaultMQPushConsumer.start();
            LOGGER.info("mq消费者启动成功,nameSrv is {}",consumerProperties.getNameSrv());
        } catch (MQClientException e) {
            throw new RuntimeException(e);
        }

    }
}
