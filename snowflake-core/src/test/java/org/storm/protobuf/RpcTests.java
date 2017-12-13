package org.storm.protobuf;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Created by fm.chen on 2017/12/1.
 */
public class RpcTests {

    private Integer port = 50051;

    private SnowflakeServer snowflakeServer = new SnowflakeServer(port);

    @Before
    public void setUp() throws IOException {
        snowflakeServer.start();
    }


    @Test
    public void rpcUsabilityTest() throws UnknownHostException, InterruptedException {
        SnowflakeClient client = new SnowflakeClient(InetAddress.getLocalHost().getHostAddress(), port);
        Long time = client.getPeerSystemInfo().getTime();
        System.out.println(Math.abs(time - System.currentTimeMillis()));
        Assert.assertTrue(Math.abs(time - System.currentTimeMillis()) <= 5);
        client.shutdown();
    }

    @After
    public void setDown() {
        snowflakeServer.shutdown();
    }
}
