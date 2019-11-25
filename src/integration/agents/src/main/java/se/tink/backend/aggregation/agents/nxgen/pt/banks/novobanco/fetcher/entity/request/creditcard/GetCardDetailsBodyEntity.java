package se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.fetcher.entity.request.creditcard;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class GetCardDetailsBodyEntity {

    public GetCardDetailsBodyEntity(boolean getCards) {
        this.getCards = getCards;
    }

    public GetCardDetailsBodyEntity(boolean getCards, String accountCardId) {
        this.getCards = getCards;
        this.accountCardId = accountCardId;
    }

    @JsonProperty("ObterCartoes")
    private boolean getCards;

    @JsonProperty("IdContaCartao")
    @JsonInclude(NON_NULL)
    private String accountCardId;
}
