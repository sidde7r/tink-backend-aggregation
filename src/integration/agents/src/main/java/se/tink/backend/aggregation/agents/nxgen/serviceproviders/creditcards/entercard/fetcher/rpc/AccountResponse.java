package se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.entercard.fetcher.rpc;

import java.util.Optional;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;

@SuppressWarnings("unused")
@JsonObject
public class AccountResponse {

    private Account account;

    public Optional<CreditCardAccount> toCreditCardAccount(User user, String accountId) {
        if (account == null) {
            throw new IllegalStateException("Account response without a card, should not happen.");
        }

        return account.toCreditCardAccount(user, accountId);
    }
}
