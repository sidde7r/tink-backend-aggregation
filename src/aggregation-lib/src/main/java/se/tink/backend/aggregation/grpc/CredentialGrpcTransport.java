package se.tink.backend.aggregation.grpc;

import io.grpc.stub.StreamObserver;
import se.tink.aggregation.grpc.CreateCredentialRequest;
import se.tink.aggregation.grpc.CreateCredentialResponse;
import se.tink.aggregation.grpc.CredentialServiceGrpc;
import se.tink.aggregation.grpc.DeleteCredentialRequest;
import se.tink.aggregation.grpc.Empty;

public class CredentialGrpcTransport extends CredentialServiceGrpc.CredentialServiceImplBase {
    @Override
    public void createCredential(CreateCredentialRequest request,
            StreamObserver<CreateCredentialResponse> responseObserver) {
        responseObserver.onNext(CreateCredentialResponse.newBuilder().setCredential(request.getCredential()).build());
        responseObserver.onCompleted();
    }

    @Override
    public void deleteCredential(DeleteCredentialRequest request, StreamObserver<Empty> responseObserver) {
        responseObserver.onCompleted();
    }
}
