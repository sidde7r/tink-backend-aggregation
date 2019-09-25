package se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v2.fetcher.creditcard.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v2.fetcher.creditcard.entities.CardInfoEntity;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v2.fetcher.creditcard.entities.CreditInfoEntity;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v2.rpc.SpankkiResponse;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CreditCardDetailsResponse extends SpankkiResponse {
    @JsonProperty private CardInfoEntity cardInfo;
    @JsonProperty private CreditInfoEntity creditInfo;
    @JsonProperty private String priceListUrl;

    @JsonIgnore
    public CardInfoEntity getCardInfo() {
        return cardInfo;
    }
}
