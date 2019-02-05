package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v20.fetcher.creditcard.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import javax.xml.bind.annotation.XmlRootElement;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v20.fetcher.creditcard.entities.CardDetailsEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v20.rpc.NordeaResponse;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@XmlRootElement
public class CardDetailsResponse extends NordeaResponse {
    @JsonProperty("getCardDetailsOut")
    protected CardDetailsEntity cardDetails;

    public CardDetailsEntity getCardDetails() {
        return cardDetails;
    }
}
