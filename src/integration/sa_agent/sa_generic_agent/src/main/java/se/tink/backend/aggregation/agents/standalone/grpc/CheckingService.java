package se.tink.backend.aggregation.agents.standalone.grpc;

import io.grpc.ManagedChannel;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.sa.services.fetch.account.FetchAccountsRequest;
import se.tink.sa.services.fetch.account.FetchAccountsServiceGrpc;

public class CheckingService {

    private final FetchAccountsServiceGrpc.FetchAccountsServiceBlockingStub
            fetchAccountsServiceBlockingStub;

    public CheckingService(final ManagedChannel channel) {
        fetchAccountsServiceBlockingStub = FetchAccountsServiceGrpc.newBlockingStub(channel);
    }

    public FetchAccountsResponse fetchCheckingAccounts(final String consetnId) {
        FetchAccountsRequest fetchAccountsRequest =
                FetchAccountsRequest.newBuilder().setConsentId(consetnId).build();
        return mapFetchAccountsResponse(
                fetchAccountsServiceBlockingStub.fetchCheckingAccounts(fetchAccountsRequest));
    }

    public FetchTransactionsResponse fetchCheckingTransactions() {
        // TODO
        return null;
    }

    private FetchAccountsResponse mapFetchAccountsResponse(
            final se.tink.sa.services.fetch.account.FetchAccountsResponse fetchAccountsResponse) {
        return new FetchAccountsResponse(mapAccountList(fetchAccountsResponse.getAccountList()));
    }

    private List<Account> mapAccountList(
            final List<se.tink.sa.services.fetch.account.TransactionalAccount> accountList) {
        return Optional.ofNullable(accountList).orElse(Collections.emptyList()).stream()
                .map(this::mapAccount)
                .collect(Collectors.toList());
    }

    private Account mapAccount(
            final se.tink.sa.services.fetch.account.TransactionalAccount transactionalAccount) {
        // TODO
        return null;
    }
}
