package org.storm;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.storm.core.ZookeeperIdGenerator;

@RunWith(SpringRunner.class)
@SpringBootTest
public class SnowflakeApplicationTests {

	@Autowired
	private ZookeeperIdGenerator zookeeperIdGenerator;

	@Test
	public void contextLoads() {
		Assert.assertEquals(zookeeperIdGenerator.getWorkId(), (Integer)5);


	}

}
