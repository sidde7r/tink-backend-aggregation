package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.fetcher;

import java.util.Collection;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.Sparebank1ApiClient;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.Sparebank1Constants;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.fetcher.entities.CreditCardAccountEntity;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.fetcher.rpc.CreditCardAccountsListResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;

public class Sparebank1CreditCardFetcher implements AccountFetcher<CreditCardAccount> {
    private final Sparebank1ApiClient apiClient;

    public Sparebank1CreditCardFetcher(Sparebank1ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<CreditCardAccount> fetchAccounts() {
        return apiClient.getAccounts(Sparebank1Constants.Urls.CREDITCARDS, CreditCardAccountsListResponse.class)
                .getCreditCards().stream()
                .filter(creditCardAccountEntity -> !creditCardAccountEntity.isAccountClosed())
                .map(CreditCardAccountEntity::toAccount)
                .collect(Collectors.toList());
    }
}
