package org.storm.configs;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
    @ConditionalOnMissingBean
    public IdGenConfig idGenConfig() {
        IdGenConfig idGenConfig = new IdGenConfig();
        return idGenConfig;
    }
}
