package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.paypal.fetcher;

import java.util.Collection;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.paypal.PayPalApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.paypal.fetcher.entities.account.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.paypal.fetcher.rpc.AccountBalanceResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

public class PayPalTransactionalAccountFetcher implements AccountFetcher<TransactionalAccount> {
    private final PayPalApiClient apiClient;

    public PayPalTransactionalAccountFetcher(PayPalApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        return apiClient.fetchAccount().getAccountList().stream()
                .map(this::toTinkAccountWithBalance)
                .collect(Collectors.toList());
    }

    private TransactionalAccount toTinkAccountWithBalance(AccountEntity account) {
        AccountBalanceResponse accountBalanceResponse = apiClient.getAccountBalance();
        return account.toTinkAccount(accountBalanceResponse);
    }
}
