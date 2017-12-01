package org.storm;

import org.junit.Assert;
import org.junit.Test;
import org.storm.utils.ByteUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.URL;
import java.util.concurrent.TimeUnit;

/**
 * Created by fm.chen on 2017/11/29.
 */
public class ByteTest {
    @Test
    public void longByteTest() {
        long cur = System.currentTimeMillis();
        byte[] bytes = ByteUtils.longToBytes(cur);
        long res = ByteUtils.bytesToLong(bytes);
        Assert.assertEquals(cur, res);
    }

    @Test
    public void timeUnitTest() {
        long res = TimeUnit.SECONDS.toMillis(3);
        Assert.assertEquals(3*1000L, res);
    }

    @Test
    public void urlTest() throws IOException {
        System.out.println(InetAddress.getLocalHost().getHostAddress());
        URL oracle = new URL("https://www.baidu.com");
        BufferedReader in = new BufferedReader(
                new InputStreamReader(oracle.openStream()));

        String inputLine;
        while ((inputLine = in.readLine()) != null)
            System.out.println(inputLine);
        in.close();
    }
}
