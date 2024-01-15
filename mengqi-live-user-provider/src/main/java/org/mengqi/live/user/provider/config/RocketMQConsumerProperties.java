package org.mengqi.live.user.provider.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "huyu.rmq.consumer")
@Data
public class RocketMQConsumerProperties {
    //rocketmq的nameServer地址
    private String nameSrv;

    //分组名称
    private String groupName;
}
