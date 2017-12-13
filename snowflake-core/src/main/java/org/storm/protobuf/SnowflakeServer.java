package org.storm.protobuf;

import io.grpc.Server;
import io.grpc.netty.NettyServerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

/**
 * Created by fm.chen on 2017/11/30.
 */
public class SnowflakeServer  {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private Integer port;

    private String host;

    private InetSocketAddress socketAddress;

    private Server server;


    public SnowflakeServer() {
        afterConstruct();
    }

    public SnowflakeServer(Integer port) {
        this.port = port;
        afterConstruct();
    }

    public SnowflakeServer(String host, Integer port) {
        this.host = host;
        this.port = port;
        afterConstruct();
    }

    public void start() throws IOException {
        server = NettyServerBuilder.forAddress(socketAddress)
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
        return socketAddress;
    }


    public void afterConstruct()  {
        if (StringUtils.isEmpty(host)) {
            socketAddress = new InetSocketAddress(port);
        } else {
            socketAddress = new InetSocketAddress(host, port);
        }
    }
}
