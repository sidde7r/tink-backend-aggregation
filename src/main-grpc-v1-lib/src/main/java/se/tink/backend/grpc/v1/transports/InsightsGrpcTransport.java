package se.tink.backend.grpc.v1.transports;

import io.grpc.stub.StreamObserver;
import java.util.List;
import javax.inject.Inject;
import se.tink.backend.auth.Authenticated;
import se.tink.backend.auth.AuthenticationContext;
import se.tink.backend.core.User;
import se.tink.backend.grpc.v1.converter.insights.InsightGrpcConverter;
import se.tink.backend.grpc.v1.interceptors.AuthenticationInterceptor;
import se.tink.backend.insights.http.dto.InsightDTO;
import se.tink.backend.main.controllers.ActionableInsightsServiceController;
import se.tink.backend.rpc.actionableinsights.GetInsightsCommand;
import se.tink.backend.rpc.actionableinsights.SelectOptionCommand;
import se.tink.grpc.v1.rpc.GetActionableInsightsRequest;
import se.tink.grpc.v1.rpc.GetActionableInsightsResponse;
import se.tink.grpc.v1.rpc.SelectInsightOptionRequest;
import se.tink.grpc.v1.rpc.SelectInsightOptionResponse;
import se.tink.grpc.v1.services.InsightsServiceGrpc;

public class InsightsGrpcTransport extends InsightsServiceGrpc.InsightsServiceImplBase {
    private ActionableInsightsServiceController actionableInsightsServiceController;

    @Inject
    public InsightsGrpcTransport(ActionableInsightsServiceController actionableInsightsServiceController) {
        this.actionableInsightsServiceController = actionableInsightsServiceController;
    }

    @Override
    @Authenticated
    public void selectOption(SelectInsightOptionRequest request,
            StreamObserver<SelectInsightOptionResponse> responseObserver) {
        AuthenticationContext authenticationContext = AuthenticationInterceptor.CONTEXT.get();
        User user = authenticationContext.getUser();

        SelectOptionCommand command = new SelectOptionCommand(user.getId(), request.getInsightsId(),
                request.getOptionId());
        actionableInsightsServiceController.selectOption(command);
        responseObserver.onNext(SelectInsightOptionResponse.newBuilder().build());
        responseObserver.onCompleted();
    }

    @Override
    @Authenticated
    public void getInsights(GetActionableInsightsRequest request,
            StreamObserver<GetActionableInsightsResponse> responseObserver) {
        AuthenticationContext authenticationContext = AuthenticationInterceptor.CONTEXT.get();
        User user = authenticationContext.getUser();

        InsightGrpcConverter converter = new InsightGrpcConverter();
        List<InsightDTO> insights = actionableInsightsServiceController.getInsights(new GetInsightsCommand(user.getId()));
        responseObserver.onNext(GetActionableInsightsResponse
                .newBuilder()
                .addAllInsights(converter.convertFrom(insights))
                .build());
        responseObserver.onCompleted();
    }
}
