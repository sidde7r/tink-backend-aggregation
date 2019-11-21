package se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.fetcher.rpc.loan;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.authenticator.entity.response.HeaderEntityWrapper;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.fetcher.entity.response.loan.GetLoanDetailsBodyEntity;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.fetcher.rpc.GetAccountsResponse;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class GetLoanDetailsResponse extends HeaderEntityWrapper {
    private static final Logger logger = LoggerFactory.getLogger(GetAccountsResponse.class);

    @JsonProperty("Body")
    private GetLoanDetailsBodyEntity body;

    public GetLoanDetailsBodyEntity getBody() {
        return body;
    }
}
