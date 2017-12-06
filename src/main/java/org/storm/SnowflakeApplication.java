package org.storm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.storm.core.ZookeeperIdGenerator;

@SpringBootApplication
public class SnowflakeApplication {


	public static void main(String[] args) {

		ApplicationContext context = SpringApplication.run(SnowflakeApplication.class, args);
		ZookeeperIdGenerator generator = context.getBean(ZookeeperIdGenerator.class);
		Integer workId = generator.getWorkId();
	}
}
