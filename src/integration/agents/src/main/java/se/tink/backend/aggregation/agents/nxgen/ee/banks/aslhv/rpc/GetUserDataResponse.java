package se.tink.backend.aggregation.agents.nxgen.ee.banks.aslhv.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.ee.banks.aslhv.entities.AccountItem;
import se.tink.backend.aggregation.agents.nxgen.ee.banks.aslhv.entities.Card;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

@JsonObject
public class GetUserDataResponse extends BaseResponse {

    @JsonProperty("cards")
    private List<Card> cards;

    @JsonProperty("accounts")
    private List<AccountItem> accounts;

    @JsonIgnore
    public Optional<List<Card>> getCards() {
        return Optional.ofNullable(cards);
    }

    @JsonIgnore
    public Collection<TransactionalAccount> getTransactionalAccounts(
            final String currentUser, final String currency, final int baseCurrencyId) {
        return accounts.stream()
                .map(account -> account.toTinkAccount(baseCurrencyId, currency, currentUser))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toSet());
    }

    @JsonIgnore
    public Collection<CreditCardAccount> getCreditCardAccounts(
            final String currentUser, final String currency, final int baseCurrencyId) {
        return accounts.stream()
                .map(
                        account ->
                                account.buildCreditCardAccount(
                                        baseCurrencyId, currency, currentUser))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toSet());
    }
}
