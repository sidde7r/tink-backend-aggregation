package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.fetcher;

import java.util.Collection;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;

@RequiredArgsConstructor
public class CreditCardAccountV31Fetcher implements AccountFetcher<CreditCardAccount> {

    private final AccountV31Fetcher<CreditCardAccount> accountV31Fetcher;

    @Override
    public Collection<CreditCardAccount> fetchAccounts() {
        return accountV31Fetcher.fetchAccounts();
    }
}
