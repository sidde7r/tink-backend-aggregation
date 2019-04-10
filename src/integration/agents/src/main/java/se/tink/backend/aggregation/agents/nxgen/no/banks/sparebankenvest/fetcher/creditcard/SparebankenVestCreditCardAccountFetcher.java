package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankenvest.fetcher.creditcard;

import java.util.Collection;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankenvest.SparebankenVestApiClient;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankenvest.fetcher.creditcard.rpc.FetchCreditCardsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;

public class SparebankenVestCreditCardAccountFetcher implements AccountFetcher<CreditCardAccount> {

    private final SparebankenVestApiClient apiClient;

    private SparebankenVestCreditCardAccountFetcher(SparebankenVestApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public static SparebankenVestCreditCardAccountFetcher create(
            SparebankenVestApiClient apiClient) {
        return new SparebankenVestCreditCardAccountFetcher(apiClient);
    }

    @Override
    public Collection<CreditCardAccount> fetchAccounts() {
        FetchCreditCardsResponse ccAccountsResponse = apiClient.fetchCreditCardAccounts();
        return ccAccountsResponse.getTinkCreditCardAccounts();
    }
}
