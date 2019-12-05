package se.tink.sa.framework.service;

import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import se.tink.sa.framework.facade.AccountInformationFacade;
import se.tink.sa.services.fetch.trans.FetchTransactionsRequest;
import se.tink.sa.services.fetch.trans.FetchTransactionsResponse;
import se.tink.sa.services.fetch.trans.FetchTransactionsServiceGrpc;

@Slf4j
@Component
public class FetchTransactionsService
        extends FetchTransactionsServiceGrpc.FetchTransactionsServiceImplBase {

    @Autowired private AccountInformationFacade accountInformationFacade;

    @Override
    public void fetchCheckingAccountsTransactions(
            FetchTransactionsRequest request,
            StreamObserver<FetchTransactionsResponse> responseObserver) {
        log.info("Incomming message: {}", request);
        FetchTransactionsResponse response =
                accountInformationFacade.fetchCheckingAccountsTransactions(request);
        log.info("Outgoing message {}", response);
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
