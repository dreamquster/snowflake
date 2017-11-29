package org.storm.utils;

import java.nio.ByteBuffer;

/**
 * Created by fm.chen on 2017/11/29.
 */
public final class ByteUtils {
    private ByteUtils() {}

    public static long bytesToLong(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.put(bytes);
        buffer.flip();//need flip
        return buffer.getLong();
    }

    public static byte[] longToBytes(long x) {
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.putLong(x);
        return buffer.array();
    }

}
