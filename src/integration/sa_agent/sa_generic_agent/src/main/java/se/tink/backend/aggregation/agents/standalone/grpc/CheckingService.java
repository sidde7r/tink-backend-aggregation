package se.tink.backend.aggregation.agents.standalone.grpc;

import io.grpc.ManagedChannel;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.sa.services.fetch.account.FetchAccountsServiceGrpc;
import se.tink.sa.services.fetch.trans.FetchTransactionsServiceGrpc;

public class CheckingService {

    private final FetchAccountsServiceGrpc.FetchAccountsServiceBlockingStub
            fetchAccountsServiceBlockingStub;
    private final FetchTransactionsServiceGrpc.FetchTransactionsServiceBlockingStub
            fetchTransactionsServiceBlockingStub;

    public CheckingService(final ManagedChannel channel) {
        fetchAccountsServiceBlockingStub = FetchAccountsServiceGrpc.newBlockingStub(channel);
        fetchTransactionsServiceBlockingStub =
                FetchTransactionsServiceGrpc.newBlockingStub(channel);
    }

    public FetchAccountsResponse fetchCheckingAccounts() {
        return AccountMapperService.mapFetchAccountsResponse(
                fetchAccountsServiceBlockingStub.fetchCheckingAccounts(
                        AccountMapperService.mapFetchAccountRequest()));
    }

    public FetchTransactionsResponse fetchCheckingTransactions() {
        return TransactionsMapperService.mapFetchTransactionsResponse(
                fetchTransactionsServiceBlockingStub.fetchCheckingAccounts(
                        TransactionsMapperService.mapFetchTransactionsRequest()));
    }
}
