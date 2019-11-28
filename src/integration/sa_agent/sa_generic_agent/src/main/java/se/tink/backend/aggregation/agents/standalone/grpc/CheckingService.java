package se.tink.backend.aggregation.agents.standalone.grpc;

import io.grpc.ManagedChannel;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.sa.services.fetch.account.FetchAccountsServiceGrpc;

public class CheckingService {

    private final FetchAccountsServiceGrpc.FetchAccountsServiceBlockingStub  fetchAccountsServiceBlockingStub;

    public CheckingService(final ManagedChannel channel) {
        fetchAccountsServiceBlockingStub = FetchAccountsServiceGrpc.newBlockingStub(channel);
    }

    public FetchAccountsResponse fetchCheckingAccounts() {
        // TODO
        return null;
    }

    public FetchTransactionsResponse fetchCheckingTransactions() {
        // TODO
        return null;
    }
}
