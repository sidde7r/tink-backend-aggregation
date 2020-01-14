package se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.fetcher;

import java.util.Collection;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.DnbApiClient;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.fetcher.entity.CardAccountEntity;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;

public class DnbCreditCardAccountFetcher implements AccountFetcher<CreditCardAccount> {
    private final DnbApiClient apiClient;

    public DnbCreditCardAccountFetcher(DnbApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<CreditCardAccount> fetchAccounts() {
        return apiClient.fetchCreditCardAccounts().getAccount().stream()
                .filter(CardAccountEntity::isCreditCardAccount)
                .map(CardAccountEntity::toCreditCardAccount)
                .collect(Collectors.toList());
    }
}
