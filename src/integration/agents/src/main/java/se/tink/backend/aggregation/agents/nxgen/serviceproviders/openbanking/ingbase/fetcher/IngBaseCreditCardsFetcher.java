package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.fetcher;

import java.util.Collection;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.IngBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.fetcher.entities.AccountEntity;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;

public class IngBaseCreditCardsFetcher implements AccountFetcher<CreditCardAccount> {

    private final IngBaseApiClient apiClient;
    private final String currency;

    public IngBaseCreditCardsFetcher(IngBaseApiClient apiClient, String currency) {
        this.apiClient = apiClient;
        this.currency = currency;
    }

    @Override
    public Collection<CreditCardAccount> fetchAccounts() {
        return apiClient.fetchAccounts().getCreditCardAccounts(currency).stream()
                .map(this::enrichAccountWithBalance)
                .collect(Collectors.toList());
    }

    private CreditCardAccount enrichAccountWithBalance(AccountEntity account) {
        return account.toTinkCreditCardAccount(apiClient.fetchBalances(account).getBalances());
    }
}
