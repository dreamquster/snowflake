package org.storm;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class SnowflakeApplication {

	@Value("${zookeeperUrl}")
	private String zookeeperUrl;

	@Bean
	public CuratorFramework curatorFrameworkClient() {
		RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
		return CuratorFrameworkFactory.builder()
					.connectString(zookeeperUrl)
					.sessionTimeoutMs(5000)
					.connectionTimeoutMs(5000)
					.retryPolicy(retryPolicy)
					.build();

	}

	public static void main(String[] args) {
		SpringApplication.run(SnowflakeApplication.class, args);
	}
}
