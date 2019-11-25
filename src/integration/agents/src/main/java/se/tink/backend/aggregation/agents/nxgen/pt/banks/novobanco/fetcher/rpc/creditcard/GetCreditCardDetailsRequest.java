package se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.fetcher.rpc.creditcard;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.authenticator.entity.request.HeaderEntity;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.fetcher.entity.request.creditcard.GetCardDetailsBodyEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class GetCreditCardDetailsRequest {

    public GetCreditCardDetailsRequest(HeaderEntity header, GetCardDetailsBodyEntity body) {
        this.header = header;
        this.body = body;
    }

    @JsonProperty("Header")
    private HeaderEntity header;

    @JsonProperty("Body")
    private GetCardDetailsBodyEntity body;
}
