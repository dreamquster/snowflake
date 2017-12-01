package org.storm.protobuf;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * Created by fm.chen on 2017/12/1.
 */
public class SnowflakeClient {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final ManagedChannel channel;

    private final IdGeneratorServiceGrpc.IdGeneratorServiceBlockingStub idGenBlockStub;

    public SnowflakeClient(String host, int port) {
        this(ManagedChannelBuilder.forAddress(host, port).usePlaintext(true).build());
    }

    /** Construct client for accessing RouteGuide server using the existing channel. */
    private SnowflakeClient(ManagedChannel channel) {
        this.channel = channel;
        idGenBlockStub = IdGeneratorServiceGrpc.newBlockingStub(channel);
    }

    public void shutdown() throws InterruptedException {
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }

    public SystemTimeResponse getPeerSystemInfo() {
        Empty empty = Empty.getDefaultInstance();
        return idGenBlockStub.getSystemInMillisOther(empty);
    }
}
