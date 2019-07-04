package se.tink.backend.aggregation.agents.nxgen.se.openbanking.seb.fetcher.creditcardaccount.rpc;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.seb.fetcher.creditcardaccount.entities.AccountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;

@JsonObject
public class CreditCardAccountsResponse {

    private List<AccountEntity> cardAccounts;

    public Collection<CreditCardAccount> toTinkAccounts() {
        return Optional.ofNullable(cardAccounts).orElse(Collections.emptyList()).stream()
                .filter(AccountEntity::isEnabled)
                .map(AccountEntity::toCreditCardAccount)
                .collect(Collectors.toList());
    }
}
