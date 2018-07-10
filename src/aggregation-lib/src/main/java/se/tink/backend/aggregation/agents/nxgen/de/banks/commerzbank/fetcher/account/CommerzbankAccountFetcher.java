package se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.fetcher.account;

import java.util.ArrayList;
import java.util.Collection;
import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.CommerzbankApiClient;
import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.entities.ResultEntity;
import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.fetcher.account.entities.ProductsEntity;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class CommerzbankAccountFetcher implements AccountFetcher<TransactionalAccount> {

    private final CommerzbankApiClient apiClient;
    public final SessionStorage sessionStorage;

    public CommerzbankAccountFetcher(CommerzbankApiClient apiClient,
            SessionStorage sessionStorage) {
        this.apiClient = apiClient;
        this.sessionStorage = sessionStorage;
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {

        Collection<TransactionalAccount> accounts = new ArrayList<>();
        ResultEntity resultEntity = apiClient.financialOverview();
        ProductsEntity productsEntity = resultEntity.getItems().get(0).getProducts().get(0);
        accounts.add(productsEntity.toTransactionalAccount());

        return accounts;
    }
}
