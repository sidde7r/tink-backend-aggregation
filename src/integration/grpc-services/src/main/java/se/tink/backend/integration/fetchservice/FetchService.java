package se.tink.backend.integration.fetchservice;

import com.google.inject.Inject;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.integration.api.rpc.CheckingAccountsResponse;
import se.tink.backend.integration.api.rpc.IntegrationRequest;
import se.tink.backend.integration.api.services.FetchServiceGrpc;
import se.tink.backend.integration.fetchservice.controller.FetchController;
import se.tink.backend.integration.fetchservice.converters.CoreToGrpcConverter;
import se.tink.backend.integration.fetchservice.converters.GrpcToCoreConverter;

public class FetchService extends FetchServiceGrpc.FetchServiceImplBase {

    private FetchController fetchController;

    @Inject
    FetchService(FetchController fetchController) {
        this.fetchController = fetchController;
    }

    @Override
    public void checkingAccounts(
            IntegrationRequest request, StreamObserver<CheckingAccountsResponse> responseObserver) {
        try {
            FetchAccountsResponse fetchAccountsResponse =
                    fetchController.execute(GrpcToCoreConverter.convert(request));
            responseObserver.onNext(CoreToGrpcConverter.convert(fetchAccountsResponse));
            responseObserver.onCompleted();

        } catch (Exception e) {
            responseObserver.onError(
                    Status.INTERNAL
                            .withDescription(e.getMessage())
                            .withCause(e.getCause())
                            .asRuntimeException());
        }
    }
}
