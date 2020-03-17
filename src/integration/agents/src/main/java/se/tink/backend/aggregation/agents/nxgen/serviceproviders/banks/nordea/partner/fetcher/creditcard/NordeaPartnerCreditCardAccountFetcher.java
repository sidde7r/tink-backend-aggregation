package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.partner.fetcher.creditcard;

import java.util.Collection;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.partner.NordeaPartnerApiClient;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;

public class NordeaPartnerCreditCardAccountFetcher implements AccountFetcher<CreditCardAccount> {

    private final NordeaPartnerApiClient apiClient;

    public NordeaPartnerCreditCardAccountFetcher(NordeaPartnerApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<CreditCardAccount> fetchAccounts() {
        return apiClient.fetchCreditCards().toTinkCreditCardAccounts();
    }
}
