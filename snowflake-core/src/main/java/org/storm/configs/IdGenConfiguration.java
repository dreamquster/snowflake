package org.storm.configs;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ConversionServiceFactoryBean;
import org.springframework.core.convert.converter.Converter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.storm.core.DBIdGenerator;
import org.storm.core.IdGeneratorRepo;
import org.storm.core.ZookeeperIdGenerator;
import org.storm.protobuf.SnowflakeServer;
import org.storm.utils.DateCompareUtils;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

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
    @ConditionalOnClass(JdbcTemplate.class)
    @ConditionalOnMissingBean(IdGeneratorRepo.class)
    public IdGeneratorRepo idGeneratorRepo(JdbcTemplate jdbcTemplate) {
        return new IdGeneratorRepo(jdbcTemplate);
    }

    @Bean
    @ConditionalOnClass(value = {IdGeneratorRepo.class, IdGenProperties.class})
    @ConditionalOnMissingBean(DBIdGenerator.class)
    public DBIdGenerator dbIdGenerator(IdGeneratorRepo idGeneratorRepo) {
        return new DBIdGenerator(idGeneratorRepo, idGenProperties.getBizTag());
    }


    @Bean(name = "conversionService")
    public ConversionServiceFactoryBean getConversionService() {
        ConversionServiceFactoryBean bean = new ConversionServiceFactoryBean();
        Set<Converter> converters = new HashSet<>();
        converters.add(new StringToDateConverter());
        bean.setConverters(converters);
        return bean;
    }

    public static class StringToDateConverter implements Converter<String, Date> {
        public Date convert(String source) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            try {
                return sdf.parse(source);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            return null;
        }

    }

    @Bean
    @ConditionalOnMissingBean(CuratorFramework.class)
    public CuratorFramework curatorFrameworkClient() {
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
        CuratorFramework client = CuratorFrameworkFactory.builder()
                .connectString(idGenProperties.getZookeeperUrl())
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
        zookeeperIdGenerator.setBaseDateTime(idGenProperties.getBaseDateTime());
        return zookeeperIdGenerator;
    }

}
