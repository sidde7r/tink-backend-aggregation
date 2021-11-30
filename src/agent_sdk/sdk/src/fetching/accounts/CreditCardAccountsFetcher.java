package se.tink.agent.sdk.fetching.accounts;

import java.util.List;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCard;

public interface CreditCardAccountsFetcher {
    List<CreditCard> fetchAccounts();
}
