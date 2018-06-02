package se.tink.backend.grpc.v1.transports;

import io.grpc.stub.StreamObserver;
import se.tink.backend.auth.Authenticated;
import se.tink.grpc.v1.rpc.PingRequest;
import se.tink.grpc.v1.rpc.PingResponse;
import se.tink.grpc.v1.services.PingServiceGrpc;

public class PingGrpcTransport extends PingServiceGrpc.PingServiceImplBase {

    @Authenticated(required = false)
    @Override
    public void ping(PingRequest request, StreamObserver<PingResponse> responseObserver) {
        responseObserver.onNext(PingResponse.newBuilder().setMessage(request.getMessage()).build());
        responseObserver.onCompleted();
    }
}
