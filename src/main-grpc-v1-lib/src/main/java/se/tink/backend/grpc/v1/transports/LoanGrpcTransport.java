package se.tink.backend.grpc.v1.transports;

import com.google.inject.Inject;
import io.grpc.stub.StreamObserver;
import se.tink.backend.auth.Authenticated;
import se.tink.backend.auth.AuthenticationContext;
import se.tink.backend.core.LoanResponse;
import se.tink.backend.grpc.v1.converter.loans.CoreLoanResponseToGrpcConverter;
import se.tink.backend.grpc.v1.interceptors.AuthenticationInterceptor;
import se.tink.backend.main.controllers.LoanServiceController;
import se.tink.backend.rpc.loans.ListLoansCommand;
import se.tink.grpc.v1.rpc.ListLoansRequest;
import se.tink.grpc.v1.rpc.ListLoansResponse;
import se.tink.grpc.v1.services.LoanServiceGrpc;

public class LoanGrpcTransport extends LoanServiceGrpc.LoanServiceImplBase {
    private final LoanServiceController loanServiceController;

    @Inject
    public LoanGrpcTransport(LoanServiceController loanServiceController) {
        this.loanServiceController = loanServiceController;
    }

    @Override
    @Authenticated
    public void listLoans(ListLoansRequest request, StreamObserver<ListLoansResponse> streamObserver) {
        AuthenticationContext authenticationContext = AuthenticationInterceptor.CONTEXT.get();

        ListLoansCommand command = new ListLoansCommand(authenticationContext.getUser().getId());

        LoanResponse loanResponse = loanServiceController.list(command);

        streamObserver.onNext(new CoreLoanResponseToGrpcConverter().convertFrom(loanResponse));
        streamObserver.onCompleted();
    }
}
