package se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.fetcher.rpc.loan;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.authenticator.entity.request.HeaderEntity;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.fetcher.entity.request.loan.GetLoanAccountsEntity;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.fetcher.entity.request.loan.GetLoanDetailsBodyEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class GetLoanDetailsRequest {
    @JsonProperty("Header")
    private HeaderEntity header;

    @JsonProperty("Body")
    private GetLoanDetailsBodyEntity body;

    public GetLoanDetailsRequest(HeaderEntity header, GetLoanDetailsBodyEntity body) {
        this.header = header;
        this.body = body;
    }
}
