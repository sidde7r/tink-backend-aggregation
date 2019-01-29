package se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.fetcher.account;

import com.google.common.base.Preconditions;
import java.util.Collection;
import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.CommerzbankApiClient;
import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.entities.ResultEntity;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

public class CommerzbankAccountFetcher implements AccountFetcher<TransactionalAccount> {

    private final CommerzbankApiClient apiClient;

    public CommerzbankAccountFetcher(CommerzbankApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        ResultEntity resultEntity = apiClient.financialOverview();
        Preconditions.checkState(resultEntity != null, "No overview found");
        return resultEntity.toTransactionalAccounts();
    }
}
