package se.tink.backend.aggregation.agents.standalone.grpc;

import io.grpc.ManagedChannel;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import se.tink.backend.aggregation.agents.standalone.GenericAgentConfiguration;
import se.tink.backend.aggregation.agents.standalone.GenericAgentConstants;
import se.tink.backend.aggregation.agents.standalone.mapper.MappingContextKeys;
import se.tink.backend.aggregation.agents.standalone.mapper.factory.MappersController;
import se.tink.backend.aggregation.agents.standalone.mapper.fetch.account.agg.FetchAccountsResponseMapper;
import se.tink.backend.aggregation.agents.standalone.mapper.fetch.trans.agg.FetchTransactionsResponseMapper;
import se.tink.backend.aggregation.agents.standalone.mapper.fetch.trans.sa.FetchTransactionsRequestMapper;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.StrongAuthenticationState;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
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
    private final PersistentStorage persistentStorage;

    public CheckingService(
            final ManagedChannel channel,
            StrongAuthenticationState strongAuthenticationState,
            GenericAgentConfiguration configuration,
            MappersController mappersController,
            PersistentStorage persistentStorage) {
        fetchAccountsServiceBlockingStub = FetchAccountsServiceGrpc.newBlockingStub(channel);
        fetchTransactionsServiceBlockingStub =
                FetchTransactionsServiceGrpc.newBlockingStub(channel);
        this.configuration = configuration;
        this.strongAuthenticationState = strongAuthenticationState;
        this.mappersController = mappersController;
        this.persistentStorage = persistentStorage;
    }

    public List<TransactionalAccount> fetchCheckingAccounts() {
        String consentId =
                persistentStorage.get(GenericAgentConstants.PersistentStorageKey.CONSENT_ID);
        MappingContext mappingContext =
                MappingContext.newInstance().put(MappingContextKeys.CONSENT_ID, consentId);
        FetchAccountsRequest fetchAccountsRequest =
                mappersController.fetchAccountsRequestMapper().map(null, mappingContext);
        se.tink.sa.services.fetch.account.FetchAccountsResponse fetchAccountsResponse =
                fetchAccountsServiceBlockingStub.fetchCheckingAccounts(fetchAccountsRequest);
        FetchAccountsResponseMapper mapper = mappersController.fetchAccountsResponseMapper();
        return mapper.map(fetchAccountsResponse);
    }

    public TransactionKeyPaginatorResponse fetchCheckingTransactions(
            TransactionalAccount account, String next) {
        String consentId =
                persistentStorage.get(GenericAgentConstants.PersistentStorageKey.CONSENT_ID);
        MappingContext mappingContext =
                MappingContext.newInstance().put(MappingContextKeys.CONSENT_ID, consentId);

        if (StringUtils.isBlank(next)) {
            mappingContext.put(MappingContextKeys.NEXT_TR_PAGE_LINK, next);
        }

        FetchTransactionsRequestMapper fetchTransactionsRequestMapper =
                mappersController.fetchTransactionsRequestMapper();

        FetchTransactionsRequest saRequest =
                fetchTransactionsRequestMapper.map(account, mappingContext);
        se.tink.sa.services.fetch.trans.FetchTransactionsResponse saResponse =
                fetchTransactionsServiceBlockingStub.fetchCheckingAccountsTransactions(saRequest);

        FetchTransactionsResponseMapper fetchTransactionsResponseMapper =
                mappersController.fetchTransactionsResponseMapper();

        TransactionKeyPaginatorResponse resp =
                fetchTransactionsResponseMapper.map(saResponse, mappingContext);
        return resp;
    }
}
