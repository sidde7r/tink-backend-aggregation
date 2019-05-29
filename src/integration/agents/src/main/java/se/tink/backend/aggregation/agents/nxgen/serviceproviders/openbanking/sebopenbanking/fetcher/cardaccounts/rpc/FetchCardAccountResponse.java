package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebopenbanking.fetcher.cardaccounts.rpc;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebopenbanking.fetcher.cardaccounts.entities.CardAccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebopenbanking.fetcher.cardaccounts.entities.ErrorEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;

@JsonObject
public class FetchCardAccountResponse {

    private ErrorEntity error;
    private List<CardAccountEntity> cardAccounts;

    public ErrorEntity getError() {
        return error;
    }

    public List<CardAccountEntity> getCardAccounts() {
        return cardAccounts;
    }

    public List<CreditCardAccount> getTransactions() {

        return Optional.ofNullable(cardAccounts)
                .map(Collection::stream)
                .orElse(Stream.empty())
                .map(CardAccountEntity::toTinkTransaction)
                .collect(Collectors.toList());
    }
}
