package se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.fetcher.rpc.creditcard;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.authenticator.entity.response.HeaderEntityWrapper;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.fetcher.entity.response.creditcard.GetCardDetailsBodyEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class GetCreditCardDetailsResponse extends HeaderEntityWrapper {
    @JsonProperty("Body")
    private GetCardDetailsBodyEntity body;

    public GetCardDetailsBodyEntity getBody() {
        return body;
    }
}
