package se.tink.backend.aggregation.agents.nxgen.fi.banks.op.fetcher.creditcards.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.op.fetcher.creditcards.entity.CardAccountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;

@JsonObject
public class CardsResponse {
    private List<CardAccountEntity> cardAccountList;

    @JsonIgnore
    public List<CreditCardAccount> getTinkCreditCards() {
        return Optional.ofNullable(cardAccountList).orElse(Collections.emptyList())
                .stream()
                .filter(CardAccountEntity::isActiveCreditCard)
                .map(CardAccountEntity::toTinkCreditCard)
                .collect(Collectors.toList());
    }

    public List<CardAccountEntity> getCardAccountList() {
        return cardAccountList;
    }
}
