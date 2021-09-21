package se.tink.backend.aggregation.agents.nxgen.se.openbanking.business.seb.fetcher.creditcards;

import java.util.Collection;
import java.util.Collections;
import lombok.NoArgsConstructor;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;

@NoArgsConstructor
public class SebCreditCardAccountFetcher implements AccountFetcher<CreditCardAccount> {

    @Override
    public Collection<CreditCardAccount> fetchAccounts() {
        return Collections.emptyList();
    }
}
