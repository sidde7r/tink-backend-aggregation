package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v17.fetcher.loan.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import javax.xml.bind.annotation.XmlRootElement;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v17.fetcher.loan.entities.LoanDetailsEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v17.rpc.NordeaResponse;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@XmlRootElement()
public class LoanDetailsResponse extends NordeaResponse {
    @JsonProperty("getLoanDetailsOut")
    private LoanDetailsEntity loanDetails;

    public LoanDetailsEntity getLoanDetails() {
        return loanDetails;
    }
}
