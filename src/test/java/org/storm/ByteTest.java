package org.storm;

import org.junit.Assert;
import org.junit.Test;
import org.storm.utils.ByteUtils;

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
}
