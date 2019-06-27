package se.tink.backend.aggregation.agents.nxgen.se.openbanking.entercard.fetcher.creditcardaccount;

import java.util.Collection;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.entercard.EnterCardApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.entercard.fetcher.entities.AccountEntity;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;

public class CreditCardAccountFetcher implements AccountFetcher<CreditCardAccount> {
    private final EnterCardApiClient apiClient;

    public CreditCardAccountFetcher(EnterCardApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<CreditCardAccount> fetchAccounts() {
        return apiClient.fetchCreditCardAccounts().getAccounts().stream()
                .filter(AccountEntity::isCreditCardAccount)
                .map(AccountEntity::toCreditCardAccount)
                .collect(Collectors.toList());
    }
}
