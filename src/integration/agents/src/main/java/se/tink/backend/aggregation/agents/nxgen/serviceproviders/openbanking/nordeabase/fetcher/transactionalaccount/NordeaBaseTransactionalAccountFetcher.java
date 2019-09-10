package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.fetcher.transactionalaccount;

import java.util.Collection;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.NordeaBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.NordeaBaseConstants.ErrorMessages;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

public class NordeaBaseTransactionalAccountFetcher
        implements AccountFetcher<TransactionalAccount>,
                TransactionKeyPaginator<TransactionalAccount, String> {
    private final NordeaBaseApiClient apiClient;

    public NordeaBaseTransactionalAccountFetcher(NordeaBaseApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {

        switch (apiClient.getConfiguration().getAgentType()) {
            case PERSONAL:
                return apiClient.getAccounts().toTinkAccounts();
            case CORPORATE:
                return apiClient.getCorporateAccounts().toTinkAccounts();
            default:
                throw new IllegalStateException(ErrorMessages.UNKNOWN_AGENT_TYPE);
        }
    }

    @Override
    public TransactionKeyPaginatorResponse<String> getTransactionsFor(
            TransactionalAccount account, String key) {

        switch (apiClient.getConfiguration().getAgentType()) {
            case PERSONAL:
                return apiClient.getTransactions(account, key);
            case CORPORATE:
                return apiClient.getCorporateTransactions(account, key);
            default:
                throw new IllegalStateException(ErrorMessages.UNKNOWN_AGENT_TYPE);
        }
    }
}
