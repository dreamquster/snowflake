package org.storm.protobuf;

import io.grpc.stub.StreamObserver;

/**
 * Created by fm.chen on 2017/12/1.
 */
public class IdGenService extends IdGeneratorServiceGrpc.IdGeneratorServiceImplBase{

    @Override
    public void getSystemInMillisOther(Empty request, StreamObserver<SystemTimeResponse> responseObserver) {
        SystemTimeResponse response = SystemTimeResponse.newBuilder()
                                    .setTime(System.currentTimeMillis())
                                    .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
