package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebopenbanking.fetcher.cardaccounts;

import java.util.Collection;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebopenbanking.SebApiClient;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.Account;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;

public class SebCardAccountFetcher<A extends Account> implements AccountFetcher<CreditCardAccount> {

    private SebApiClient client;

    public SebCardAccountFetcher(SebApiClient client) {
        this.client = client;
    }

    @Override
    public Collection<CreditCardAccount> fetchAccounts() {
        return client.fetchCreditAccounts();
    }
}
