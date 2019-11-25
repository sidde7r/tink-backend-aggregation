package se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.fetcher.entity.response.creditcard;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ContextCardsEntity {
    @JsonProperty("ContasCartao")
    private CardAccountsEntity cardAccounts;

    public CardAccountsEntity getCardAccounts() {
        return cardAccounts;
    }
}
