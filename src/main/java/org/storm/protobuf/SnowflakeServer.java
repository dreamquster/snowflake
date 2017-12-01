package org.storm.protobuf;

import io.grpc.Server;
import io.grpc.netty.NettyServerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Created by fm.chen on 2017/11/30.
 */
@Component
public class SnowflakeServer  {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private int port = 50051;

    private Server server;


    public SnowflakeServer() {

    }

    public void start() throws IOException {
        server = NettyServerBuilder.forPort(port)
                .addService(new IdGenService())
                .build().start();
        logger.info("Server started, listening on " + port);
        Runtime.getRuntime().addShutdownHook(new Thread(){
            @Override
            public void run() {
                // Use stderr here since the logger may have been reset by its JVM shutdown hook.
                System.err.println("*** shutting down gRPC server since JVM is shutting down");
                SnowflakeServer.this.stop();
                System.err.println("*** server shut down");
            }
        });
    }

    private void stop() {
        if (null != server) {
            server.shutdown();
        }
    }

}
