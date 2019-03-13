package se.tink.backend.integration.fetchservice;

import io.grpc.stub.StreamObserver;
import java.util.Collections;
import se.tink.backend.integration.api.models.AgentState;
import se.tink.backend.integration.api.models.IntegrationAccounts;
import se.tink.backend.integration.api.rpc.CheckingAccountsResponse;
import se.tink.backend.integration.api.rpc.IntegrationRequest;
import se.tink.backend.integration.api.rpc.IntegrationResponse;
import se.tink.backend.integration.api.services.FetchServiceGrpc;

public class FetchService extends FetchServiceGrpc.FetchServiceImplBase {
    @Override
    public void checkingAccounts(IntegrationRequest request,
            StreamObserver<CheckingAccountsResponse> responseObserver) {
    responseObserver.onNext(
        CheckingAccountsResponse.newBuilder().setIntegrationInfo(
                IntegrationResponse.newBuilder()
                        .setOperationId(request.getOperationId())
                        .setState(
                                AgentState.newBuilder()
                                        .setState("STEP_1")
                                        .build()))
            .addAllAccounts(Collections.emptyList())
            .addAccounts(IntegrationAccounts.newBuilder().build())
            .build());
        responseObserver.onCompleted();
    }
}
