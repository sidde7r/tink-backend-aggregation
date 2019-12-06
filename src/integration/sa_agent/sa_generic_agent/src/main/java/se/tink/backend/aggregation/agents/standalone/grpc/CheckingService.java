package se.tink.backend.aggregation.agents.standalone.grpc;

import io.grpc.ManagedChannel;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.standalone.GenericAgentConfiguration;
import se.tink.backend.aggregation.agents.standalone.mapper.fetch.account.agg.FetchAccountsResponseMapper;
import se.tink.backend.aggregation.agents.standalone.mapper.fetch.account.agg.FetchAccountsResponseMapperFactory;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.StrongAuthenticationState;
import se.tink.sa.services.fetch.account.FetchAccountsRequest;
import se.tink.sa.services.fetch.account.FetchAccountsServiceGrpc;
import se.tink.sa.services.fetch.trans.FetchTransactionsServiceGrpc;

public class CheckingService {

    private final FetchAccountsServiceGrpc.FetchAccountsServiceBlockingStub
            fetchAccountsServiceBlockingStub;
    private final FetchTransactionsServiceGrpc.FetchTransactionsServiceBlockingStub
            fetchTransactionsServiceBlockingStub;
    private final GenericAgentConfiguration configuration;
    private final StrongAuthenticationState strongAuthenticationState;

    public CheckingService(
            final ManagedChannel channel,
            StrongAuthenticationState strongAuthenticationState,
            GenericAgentConfiguration configuration) {
        fetchAccountsServiceBlockingStub = FetchAccountsServiceGrpc.newBlockingStub(channel);
        fetchTransactionsServiceBlockingStub =
                FetchTransactionsServiceGrpc.newBlockingStub(channel);
        this.configuration = configuration;
        this.strongAuthenticationState = strongAuthenticationState;
    }

    public FetchAccountsResponse fetchCheckingAccounts() {
        FetchAccountsRequest fetchAccountsRequest = null;
        se.tink.sa.services.fetch.account.FetchAccountsResponse fetchAccountsResponse =
                fetchAccountsServiceBlockingStub.fetchCheckingAccounts(fetchAccountsRequest);
        FetchAccountsResponseMapper mapper =
                FetchAccountsResponseMapperFactory.buildFetchAccountsResponseMapper();
        return mapper.map(fetchAccountsResponse);
    }

    public FetchTransactionsResponse fetchCheckingTransactions() {
        return TransactionsMapperService.mapFetchTransactionsResponse(
                fetchTransactionsServiceBlockingStub.fetchCheckingAccountsTransactions(
                        TransactionsMapperService.mapFetchTransactionsRequest()));
    }
}
