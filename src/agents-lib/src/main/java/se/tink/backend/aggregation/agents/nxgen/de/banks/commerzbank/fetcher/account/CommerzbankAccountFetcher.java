package se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.fetcher.account;

import com.google.common.base.Preconditions;
import java.util.Collection;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.CommerzbankApiClient;
import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.entities.ResultEntity;
import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.fetcher.account.entities.ProductsEntity;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;

public class CommerzbankAccountFetcher implements AccountFetcher<TransactionalAccount> {

    private final CommerzbankApiClient apiClient;

    public CommerzbankAccountFetcher(CommerzbankApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {

        ResultEntity resultEntity = apiClient.financialOverview();
        apiClient.logMultibankingProducts();

        Preconditions.checkState(resultEntity != null, "No overview found");
        return resultEntity.getItems().get(0).getProducts().stream()
                .filter(productsEntity -> productsEntity
                        .getProductType()
                        .getDisplayCategoryIndex() == 1)
                .map(ProductsEntity::toTransactionalAccount)
                .collect(Collectors.toList());
    }
}
