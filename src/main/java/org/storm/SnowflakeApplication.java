package org.storm;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;

@SpringBootApplication
@PropertySource("file:${data-dir}/app.properties")
public class SnowflakeApplication {

	@Value("${zookeeperUrl}")
	private String zookeeperUrl;

	@Value("${base-dir}")
	private String baseDir;



	@Bean
	public CuratorFramework curatorFrameworkClient() {
		RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
		CuratorFramework client = CuratorFrameworkFactory.builder()
					.connectString(zookeeperUrl)
					.sessionTimeoutMs(5000)
					.connectionTimeoutMs(5000)
					.retryPolicy(retryPolicy)
					.namespace(baseDir)
					.build();
		client.start();
		return client;

	}

	public static void main(String[] args) {

		ApplicationContext context = SpringApplication.run(SnowflakeApplication.class, args);
		ZookeeperIdGenerator generator = context.getBean(ZookeeperIdGenerator.class);
		Integer workId = generator.getWorkId();
	}
}
