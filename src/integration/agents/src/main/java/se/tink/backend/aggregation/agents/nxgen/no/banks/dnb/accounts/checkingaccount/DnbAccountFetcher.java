package se.tink.backend.aggregation.agents.nxgen.no.banks.dnb.accounts.checkingaccount;

import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.no.banks.dnb.DnbApiClient;
import se.tink.backend.aggregation.agents.nxgen.no.banks.dnb.DnbConstants;
import se.tink.backend.aggregation.agents.nxgen.no.banks.dnb.accounts.checkingaccount.entities.AccountDetailsEntity;
import se.tink.backend.aggregation.agents.nxgen.no.banks.dnb.accounts.checkingaccount.rpc.AccountListResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

public class DnbAccountFetcher implements AccountFetcher<TransactionalAccount> {
    private DnbApiClient apiClient;

    public DnbAccountFetcher(DnbApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        AccountListResponse accountListResponse = apiClient.fetchAccounts();

        return accountListResponse.getAccountList().stream()
                .filter(
                        account ->
                                !Objects.equals(
                                        account.getProductNumber(),
                                        DnbConstants.ProductNumber.StockAccount))
                .map(AccountDetailsEntity::toTransactionalAccount)
                .collect(Collectors.toList());
    }
}
