package se.tink.agent.sdk.fetching.accounts;

import java.util.List;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;

public interface CreditCardAccountsFetcher {
    List<CreditCardAccount> fetchAccounts();
}
