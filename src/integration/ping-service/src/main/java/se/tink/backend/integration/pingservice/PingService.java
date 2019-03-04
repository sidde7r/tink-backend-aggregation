package se.tink.backend.integration.pingservice;

import io.grpc.stub.StreamObserver;
import se.tink.backend.integration.api.PingServiceGrpc;
import se.tink.backend.integration.api.grpc.PingRequest;
import se.tink.backend.integration.api.grpc.PingResponse;

public class PingService extends PingServiceGrpc.PingServiceImplBase {
    @Override
    public void ping(PingRequest request,
                     StreamObserver<PingResponse> responseObserver) {
        responseObserver.onNext(PingResponse.newBuilder().setMessage("pong " + request.getMessage()).build());
        responseObserver.onCompleted();
    }
}
