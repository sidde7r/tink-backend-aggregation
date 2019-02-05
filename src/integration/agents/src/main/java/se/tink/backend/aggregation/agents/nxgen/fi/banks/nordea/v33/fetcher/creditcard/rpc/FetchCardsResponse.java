package se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v33.fetcher.creditcard.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v33.NordeaFIConstants;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v33.fetcher.creditcard.entities.CardsEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.CreditCardAccount;

@JsonObject
public class FetchCardsResponse {
    @JsonProperty
    private List<CardsEntity> cards;

    public List<CreditCardAccount> toTinkCards() {
        return cards.stream()
                .filter(entity -> NordeaFIConstants.ACCOUNT_TYPE_MAPPER.translate(entity.getCardCategory()).equals(
                        Optional.of(AccountTypes.CREDIT_CARD)))
                .map(CardsEntity::toTinkCard)
                .collect(Collectors.toList());
    }
}
