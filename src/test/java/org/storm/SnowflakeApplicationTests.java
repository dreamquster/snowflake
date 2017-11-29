package org.storm;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class SnowflakeApplicationTests {

	@Autowired
	private ZookeeperIdGenerator zookeeperIdGenerator;

	@Test
	public void contextLoads() {
		Long id = zookeeperIdGenerator.snowflakeId(32, 32);


	}

}
