package org.mengqi.live.user.provider.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 生产者的配置信息
 *
 */

@ConfigurationProperties(prefix = "huyu.rmq.producer")
@Configuration
@Data
public class RocketMQProducerProperties {

    //rocketmq的nameServer地址
    private String nameSrv;

    //分组名称
    private String groupName;

    //消息重发次数
    private int retryTimes;

    private int sendTimeOut;
}
