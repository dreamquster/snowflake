package org.storm.configs;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.storm.core.ZookeeperIdGenerator;
import org.storm.protobuf.SnowflakeServer;

import java.io.IOException;

/**
 * Created by fm.chen on 2017/12/5.
 */
@Configuration
@ConditionalOnClass(IdGenProperties.class)
@EnableConfigurationProperties(IdGenProperties.class)
public class IdGenConfiguration {

    @Autowired
    private IdGenProperties idGenProperties;


    @Bean
    @ConditionalOnMissingBean(CuratorFramework.class)
    public CuratorFramework curatorFrameworkClient() {
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
        CuratorFramework client = CuratorFrameworkFactory.builder()
                .connectString(idGenProperties.getZookeeperUrl())
                .connectionTimeoutMs(5000)
                .retryPolicy(retryPolicy)
                .namespace(idGenProperties.getBaseUrl())
                .build();
        client.start();
        return client;

    }


    @Bean
    @ConditionalOnMissingBean(SnowflakeServer.class)
    public SnowflakeServer snowflakeServer() throws IOException {
        SnowflakeServer snowflakeServer = new SnowflakeServer(idGenProperties.getRpcHost(),
                idGenProperties.getRpcPort());
        return snowflakeServer;

    }

    @Bean
    @ConditionalOnMissingBean(PropertiesFileService.class)
    public PropertiesFileService propertiesFileService() throws IOException {
        PropertiesFileService propertiesFileService = new PropertiesFileService(idGenProperties.getDataDir());
        return propertiesFileService;
    }

    @Bean
    @ConditionalOnMissingBean(ZookeeperIdGenerator.class)
    public ZookeeperIdGenerator zookeeperIdGenerator(CuratorFramework zkClient,
                                                     PropertiesFileService propertiesFileService,
                                                     SnowflakeServer snowflakeServer) {
        ZookeeperIdGenerator zookeeperIdGenerator = new ZookeeperIdGenerator(zkClient, propertiesFileService, snowflakeServer);
        return zookeeperIdGenerator;
    }

}
