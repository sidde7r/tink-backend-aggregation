package se.tink.sa.framework.service;

import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import se.tink.sa.framework.facade.AccountInformationFacade;
import se.tink.sa.services.fetch.account.FetchAccountsRequest;
import se.tink.sa.services.fetch.account.FetchAccountsResponse;
import se.tink.sa.services.fetch.account.FetchAccountsServiceGrpc;

@Slf4j
@Component
public class FetchAccountsService extends FetchAccountsServiceGrpc.FetchAccountsServiceImplBase {

    @Autowired private AccountInformationFacade accountInformationFacade;

    @Override
    public void fetchCheckingAccounts(
            FetchAccountsRequest request, StreamObserver<FetchAccountsResponse> responseObserver) {
        log.info("Incomming message: {}", request);
        FetchAccountsResponse response = accountInformationFacade.fetchCheckingAccounts(request);
        log.info("Outgoing message {}", response);
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
