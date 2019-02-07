package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers;

import java.util.Collection;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.DanskeBankApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.rpc.ListAccountsRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.rpc.ListAccountsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;

public class DanskeBankCreditCardFetcher implements AccountFetcher<CreditCardAccount> {

    private final DanskeBankApiClient apiClient;
    private final String languageCode;

    public DanskeBankCreditCardFetcher(DanskeBankApiClient apiClient, String languageCode) {
        this.apiClient = apiClient;
        this.languageCode = languageCode;
    }

    @Override
    public Collection<CreditCardAccount> fetchAccounts() {
        ListAccountsResponse listAccountsResponse = this.apiClient.listAccounts(
                ListAccountsRequest.createFromLanguageCode(this.languageCode));

        return listAccountsResponse.toTinkCreditCardAccounts();
    }
}
