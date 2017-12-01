package org.storm.protobuf;

import io.grpc.Server;
import io.grpc.netty.NettyServerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;

/**
 * Created by fm.chen on 2017/11/30.
 */
@Component
public class SnowflakeServer  {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Value("${rpc.port ?: 50051}")
    private Integer port = 50051;

    private Server server;


    public SnowflakeServer() {
    }

    public SnowflakeServer(Integer port) {
        this.port = port;
    }

    public void start() throws IOException {
        server = NettyServerBuilder.forPort(port)
                .addService(new IdGenService())
                .build().start();
        logger.info("Server started, listening on " + port);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            // Use stderr here since the logger may have been reset by its JVM shutdown hook.
            System.err.println("*** shutting down gRPC server since JVM is shutting down");
            SnowflakeServer.this.shutdown();
            System.err.println("*** server shut down");
        }));
    }

    public void shutdown() {
        if (null != server) {
            server.shutdown();
        }
    }

    public InetSocketAddress getAddress() {
        return new InetSocketAddress(port);
    }
}
