package se.tink.backend.aggregation.agents.standalone.grpc;

import io.grpc.ManagedChannel;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.standalone.GenericAgentConfiguration;
import se.tink.backend.aggregation.agents.standalone.mapper.factory.MappersController;
import se.tink.backend.aggregation.agents.standalone.mapper.fetch.account.agg.FetchAccountsResponseMapper;
import se.tink.backend.aggregation.agents.standalone.mapper.fetch.trans.agg.FetchTransactionsResponseMapper;
import se.tink.backend.aggregation.agents.standalone.mapper.fetch.trans.sa.FetchTransactionsRequestMapper;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.StrongAuthenticationState;
import se.tink.sa.common.mapper.MappingContext;
import se.tink.sa.services.fetch.account.FetchAccountsRequest;
import se.tink.sa.services.fetch.account.FetchAccountsServiceGrpc;
import se.tink.sa.services.fetch.trans.FetchTransactionsRequest;
import se.tink.sa.services.fetch.trans.FetchTransactionsServiceGrpc;

public class CheckingService {

    private final FetchAccountsServiceGrpc.FetchAccountsServiceBlockingStub
            fetchAccountsServiceBlockingStub;
    private final FetchTransactionsServiceGrpc.FetchTransactionsServiceBlockingStub
            fetchTransactionsServiceBlockingStub;
    private final GenericAgentConfiguration configuration;
    private final StrongAuthenticationState strongAuthenticationState;
    private final MappersController mappersController;

    public CheckingService(
            final ManagedChannel channel,
            StrongAuthenticationState strongAuthenticationState,
            GenericAgentConfiguration configuration,
            MappersController mappersController) {
        fetchAccountsServiceBlockingStub = FetchAccountsServiceGrpc.newBlockingStub(channel);
        fetchTransactionsServiceBlockingStub =
                FetchTransactionsServiceGrpc.newBlockingStub(channel);
        this.configuration = configuration;
        this.strongAuthenticationState = strongAuthenticationState;
        this.mappersController = mappersController;
    }

    public FetchAccountsResponse fetchCheckingAccounts() {
        FetchAccountsRequest fetchAccountsRequest = null;
        se.tink.sa.services.fetch.account.FetchAccountsResponse fetchAccountsResponse =
                fetchAccountsServiceBlockingStub.fetchCheckingAccounts(fetchAccountsRequest);
        FetchAccountsResponseMapper mapper = mappersController.fetchAccountsResponseMapper();
        return mapper.map(fetchAccountsResponse);
    }

    public FetchTransactionsResponse fetchCheckingTransactions() {
        FetchTransactionsRequestMapper fetchTransactionsRequestMapper =
                mappersController.fetchTransactionsRequestMapper();
        FetchTransactionsRequest saRequest =
                fetchTransactionsRequestMapper.map(null, MappingContext.newInstance());
        se.tink.sa.services.fetch.trans.FetchTransactionsResponse saResponse =
                fetchTransactionsServiceBlockingStub.fetchCheckingAccountsTransactions(saRequest);
        FetchTransactionsResponseMapper fetchTransactionsResponseMapper =
                mappersController.fetchTransactionsResponseMapper();
        return fetchTransactionsResponseMapper.map(saResponse, MappingContext.newInstance());
    }
}
