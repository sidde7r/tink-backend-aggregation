package se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.fetcher.account;

import com.google.common.base.Preconditions;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;
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

        ResultEntity resultEntity = apiClient.financialOverview();

        Preconditions.checkState(resultEntity != null, Collections.EMPTY_LIST);
        return resultEntity.getItems().get(0).getProducts().stream()
                .filter(productsEntity -> productsEntity
                        .getProductType()
                        .getDisplayCategoryIndex() == 1)
                .map(ProductsEntity::toTransactionalAccount)
                .collect(Collectors.toCollection(ArrayList::new));
    }
}
