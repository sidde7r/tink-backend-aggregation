package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.fetcher;

import java.util.Collection;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.Sparebank1ApiClient;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.Sparebank1Constants;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.fetcher.entities.CreditCardAccountEntity;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.fetcher.rpc.CreditCardAccountsListResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.http.URL;

public class Sparebank1CreditCardFetcher implements AccountFetcher<CreditCardAccount> {
    private final Sparebank1ApiClient apiClient;
    private final String bankName;

    public Sparebank1CreditCardFetcher(Sparebank1ApiClient apiClient, String bankKey) {
        this.apiClient = apiClient;
        this.bankName = bankKey.substring(4);
    }

    @Override
    public Collection<CreditCardAccount> fetchAccounts() {
        URL url = Sparebank1Constants.Urls.CREDITCARDS.parameter(Sparebank1Constants.UrlParameter.BANK_NAME, bankName);

        return apiClient.get(url, CreditCardAccountsListResponse.class)
                .getCreditCards()
                .stream()
                .map(CreditCardAccountEntity::toAccount)
                .collect(Collectors.toList());
    }
}
