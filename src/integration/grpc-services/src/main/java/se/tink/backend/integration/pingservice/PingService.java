package se.tink.backend.integration.pingservice;

import io.grpc.stub.StreamObserver;
import se.tink.backend.integration.api.rpc.PingRequest;
import se.tink.backend.integration.api.rpc.PingResponse;
import se.tink.backend.integration.api.services.PingServiceGrpc;

public class PingService extends PingServiceGrpc.PingServiceImplBase {
    @Override
    public void ping(PingRequest request, StreamObserver<PingResponse> responseObserver) {
        responseObserver.onNext(
                PingResponse.newBuilder().setMessage("pong " + request.getMessage()).build());
        responseObserver.onCompleted();
    }
}
