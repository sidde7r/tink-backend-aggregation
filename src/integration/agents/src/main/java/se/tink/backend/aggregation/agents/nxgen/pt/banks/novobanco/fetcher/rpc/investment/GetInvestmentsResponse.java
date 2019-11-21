package se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.fetcher.rpc.investment;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.authenticator.entity.response.HeaderEntityWrapper;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.fetcher.entity.response.investment.GetInvestmentsBodyEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class GetInvestmentsResponse extends HeaderEntityWrapper {

    @JsonProperty("Body")
    private GetInvestmentsBodyEntity body;

    public GetInvestmentsBodyEntity getBody() {
        return body;
    }
}
