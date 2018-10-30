package se.tink.backend.aggregation.agents.nxgen.ee.banks.aslhv.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.ee.banks.aslhv.AsLhvConstants;
import se.tink.backend.aggregation.agents.nxgen.ee.banks.aslhv.entities.Account;
import se.tink.backend.aggregation.agents.nxgen.ee.banks.aslhv.entities.Card;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.entity.HolderName;
import se.tink.backend.core.Amount;

@JsonObject
public class GetUserDataResponse extends BaseResponse {

    @JsonProperty("cards")
    private List<Card> cards;

    @JsonProperty("accounts")
    private List<Account> accounts;

    public List<Card> getCards() {
        return cards;
    }

    public Collection<TransactionalAccount> getAccounts(
            final String currentUser,
            final String currency,
            final int baseCurrencyId) {
        Collection<TransactionalAccount> result = new HashSet<>();
        // TODO use collections.stream here
        for (Account account : accounts) {
            Amount balance = new Amount(currency, account.getBalance(baseCurrencyId));
            if (AsLhvConstants.ACCOUNT_TYPE_MAPPER.isTransactionalAccount(account.getType())) {
                TransactionalAccount transactionalAccount =
                        TransactionalAccount.builder(account.getType(), account.getIban(), balance)
                                .setName(account.getName().isEmpty() ? account.getIban() : account.getName())
                                .setHolderName(new HolderName(currentUser))
                                .setAccountNumber(account.getPortfolioId())
                                .build();
                result.add(transactionalAccount);
            }
        }
        return result;
    }
}
