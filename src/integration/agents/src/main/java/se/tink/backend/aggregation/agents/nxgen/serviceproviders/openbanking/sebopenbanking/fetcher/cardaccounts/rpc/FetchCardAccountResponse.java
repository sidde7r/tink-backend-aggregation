package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebopenbanking.fetcher.cardaccounts.rpc;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebopenbanking.fetcher.cardaccounts.entities.CardAccount;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebopenbanking.fetcher.cardaccounts.entities.Error;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;

@JsonObject
public class FetchCardAccountResponse {

    private Error error;
    private List<CardAccount> cardAccounts;

    public Error getError() {
        return error;
    }

    public List<CardAccount> getCardAccounts() {
        return cardAccounts;
    }

    public List<CreditCardAccount> getTransactions() {

        return Optional.ofNullable(cardAccounts)
                .map(Collection::stream)
                .orElse(Stream.empty())
                .map(CardAccount::toTinkTransaction)
                .collect(Collectors.toList());
    }
}
