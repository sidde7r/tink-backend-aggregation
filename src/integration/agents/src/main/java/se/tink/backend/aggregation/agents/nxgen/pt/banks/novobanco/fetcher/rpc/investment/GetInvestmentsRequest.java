package se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.fetcher.rpc.investment;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.authenticator.entity.request.HeaderEntity;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.fetcher.entity.request.investment.GetInvestmentsBodyEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class GetInvestmentsRequest {

    public GetInvestmentsRequest(HeaderEntity header, GetInvestmentsBodyEntity body) {
        this.header = header;
        this.body = body;
    }

    @JsonProperty("Header")
    private HeaderEntity header;

    @JsonProperty("Body")
    @JsonInclude(NON_NULL)
    private GetInvestmentsBodyEntity body;
}
