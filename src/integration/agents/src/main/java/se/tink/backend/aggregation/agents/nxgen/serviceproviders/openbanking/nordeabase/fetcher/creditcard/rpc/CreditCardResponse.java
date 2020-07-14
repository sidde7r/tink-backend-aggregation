package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.fetcher.creditcard.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.fetcher.creditcard.entities.CardEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.fetcher.creditcard.entities.CardsEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.fetcher.creditcard.entities.GroupHeaderEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CreditCardResponse {
    @JsonProperty("group_header")
    private GroupHeaderEntity groupHeader;

    @JsonProperty("response")
    private CardsEntity creditCard;

    public GroupHeaderEntity getGroupHeader() {
        return groupHeader;
    }

    public CardsEntity getCreditCard() {
        return creditCard;
    }

    public List<CardEntity> getCards() {
        return creditCard.getCards();
    }
}
